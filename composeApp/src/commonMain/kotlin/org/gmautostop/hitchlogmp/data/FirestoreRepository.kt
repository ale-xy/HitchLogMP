package org.gmautostop.hitchlogmp.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.toTimestamp
import org.lighthousegames.logging.logging
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private class NotAuthenticatedException : Exception("Not authenticated")
private class NotFoundException(id: String) : Exception("Document $id doesn't exist")

@OptIn(ExperimentalUuidApi::class)
class FirestoreRepository(
    private val authService: AuthService,
    private val syncTracker: FirestoreSyncTracker
) : Repository {
    private val firestore = Firebase.firestore

    private val logsRef = firestore.collection("logs")
    private fun logRecordsRef(logId: String) = firestore.collection("logs/$logId/records")

    init {
        firestore.setLoggingEnabled(true)
    }

    private fun <T> repositoryFlow(isWrite: Boolean = false, body: suspend () -> T): Flow<Response<T>> =
        flow {
            emit(Response.Loading())
            val result = body()
            if (isWrite) {
                syncTracker.trackWrite()
            }
            emit(Response.Success(result))
        }.catch { error ->
            val appError = when (error) {
                is NotAuthenticatedException -> AppError.NotAuthenticated
                is NotFoundException -> AppError.NotFound
                else -> AppError.NetworkError(error.message ?: "Unknown error")
            }
            log.e(err = error) { appError.displayMessage }
            emit(Response.Failure(appError))
        }.flowOn(Dispatchers.IO)

    private suspend inline fun <T> firestoreWrite(
        operationName: String,
        crossinline block: suspend () -> T
    ) {
        try {
            withTimeout(100) {
                block()
            }
        } catch (e: TimeoutCancellationException) {
            log.d { "$operationName: operation timed out, but write is queued locally" }
        }
    }

    override fun getLogs() = logsRef
        .where { "userId" equalTo authService.currentUser.value?.id }
        .orderBy("creationTime", Direction.DESCENDING)
        .snapshots.map {
            it.documents.map { document ->
                document.data<HitchLog>()
            }
        }.map<List<HitchLog>, Response<List<HitchLog>>> {
            Response.Success(it)
        }.catch { error ->
            log.e(err = error) { error.message ?: "getLogs error" }
            emit(Response.Failure(AppError.NetworkError(error.message ?: "getLogs error")))
        }.flowOn(Dispatchers.IO)


    override fun getLog(logId: String): Flow<Response<HitchLog>> =
        logsRef.document(logId).snapshots
            .map { snapshot ->
                if (!snapshot.exists) throw NotFoundException(logId)
                snapshot.data<HitchLog>()
            }
            .map<HitchLog, Response<HitchLog>> {
                Response.Success(it)
            }
            .catch { error ->
                log.e(err = error) { error.message ?: "getLog error" }
                val appError = when (error) {
                    is NotFoundException -> AppError.NotFound
                    else -> AppError.NetworkError(error.message ?: "getLog error")
                }
                emit(Response.Failure(appError))
            }
            .flowOn(Dispatchers.IO)

    override fun addLog(log: HitchLog) = repositoryFlow(isWrite = true) {
        val userId = authService.currentUser.value?.id
            ?: throw NotAuthenticatedException()
        val id = Uuid.random().toString()
        firestoreWrite("addLog") {
            logsRef.document(id).set(log.copy(id = id, userId = userId))
        }
    }

    override fun updateLog(log: HitchLog) = repositoryFlow(isWrite = true) {
        firestoreWrite("updateLog") {
            logsRef.document(log.id).set(log)
        }
    }

    override fun deleteLog(id: String) = repositoryFlow(isWrite = true) {
        firestoreWrite("deleteLog") {
            logsRef.document(id).delete()
        }
    }

    override fun getLogRecords(logId: String) =
        logRecordsRef(logId).orderBy("timestamp").snapshots().map { snapshot ->
            snapshot.documents.map { document ->
                document.data<FirestoreHitchLogRecord>().toHitchLogRecord()
            }
        }.map<List<HitchLogRecord>, Response<List<HitchLogRecord>>> {
            Response.Success(it)
        }.catch { error ->
            log.e(err = error) { error.message ?: "getLogRecords error" }
            emit(Response.Failure(AppError.NetworkError(error.message ?: "getLogRecords error")))
        }

    override fun getRecord(logId: String, recordId: String) = repositoryFlow {
        with(logRecordsRef(logId).document(recordId).get(Source.CACHE)) {
            when {
                !exists -> throw NotFoundException(recordId)
                else -> data<FirestoreHitchLogRecord>().toHitchLogRecord()
            }
        }
    }

    override fun addRecord(logId: String, record: HitchLogRecord) = repositoryFlow(isWrite = true) {
        val id = Uuid.random().toString()

        val firestoreRecord = FirestoreHitchLogRecord(
            record,
            id,
            getNextTime(logId, record.time.toTimestamp())
        )

        firestoreWrite("addRecord") {
            logRecordsRef(logId).document(id).set(firestoreRecord)
        }
    }

    override fun updateRecord(logId: String, record: HitchLogRecord) = repositoryFlow(isWrite = true) {
        val existing = logRecordsRef(logId).document(record.id).get(Source.CACHE).data<FirestoreHitchLogRecord>()

        val updatedRecord = if (existing.timestamp == record.time.toTimestamp()) {
            FirestoreHitchLogRecord(record)
        } else {
            FirestoreHitchLogRecord(
                from = record,
                timestamp = getNextTime(logId, record.time.toTimestamp())
            )
        }
        
        firestoreWrite("updateRecord") {
            logRecordsRef(logId).document(updatedRecord.id).set(updatedRecord)
        }
    }

    override fun deleteRecord(logId: String, record: HitchLogRecord) = repositoryFlow(isWrite = true) {
        firestoreWrite("deleteRecord") {
            logRecordsRef(logId).document(record.id).delete()
        }
    }

    override fun saveRecord(logId: String, record: HitchLogRecord) =
        when {
            record.id.isEmpty() -> addRecord(logId, record)
            else -> updateRecord(logId, record)
        }

    private suspend fun getNextTime(logId: String, enteredTime: Timestamp): Timestamp {
        return logRecordsRef(logId)
                    .where { "timestamp" greaterThanOrEqualTo enteredTime }
                    .where { "timestamp" lessThan enteredTime.addMinute() }
                    .get(Source.CACHE)
                    .documents.map { it.data<FirestoreHitchLogRecord>()
                    }.maxByOrNull { it.timestamp.seconds }?.timestamp?.addSecond()
            ?: enteredTime
    }

    private fun LocalDateTime.addSecond(): LocalDateTime =
        toInstant(TimeZone.currentSystemDefault()).plus(1, DateTimeUnit.SECOND).localTZDateTime()
    private fun LocalDateTime.addMinute(): LocalDateTime =
        toInstant(TimeZone.currentSystemDefault()).plus(1, DateTimeUnit.MINUTE).localTZDateTime()

    private fun Timestamp.addSecond(): Timestamp = Timestamp(seconds + 1, nanoseconds)
    private fun Timestamp.addMinute(): Timestamp = Timestamp(seconds + 60, nanoseconds)

    companion object {
        val log = logging()
    }
}
