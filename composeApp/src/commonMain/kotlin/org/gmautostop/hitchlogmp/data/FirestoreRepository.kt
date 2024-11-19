package org.gmautostop.hitchlogmp.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.toTimestamp
import org.lighthousegames.logging.logging
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class FirestoreRepository(
    private val authService: AuthService
) : Repository {
    private val firestore = Firebase.firestore

    private val logsRef = firestore.collection("logs")
    private fun logRecordsRef(logId: String) = firestore.collection("logs/$logId/records")

    init {
        firestore.setLoggingEnabled(true)
    }

    override fun userId() = authService.currentUser.value?.id

    private fun <T> repositoryFlow(body:suspend () -> T): Flow<Response<T>> =
        flow {
            log.d {"repositoryFlow loading" }
            emit(Response.Loading())
            val result = body().also {
                log.d { "repositoryFlow success $it" }
            }
            emit(Response.Success(result))
        }.catch { error ->
            error.message?.let { errorMessage ->
                log.e(err = error) { errorMessage }
                emit(Response.Failure(errorMessage))
            }
        }.flowOn(Dispatchers.IO)

    override fun getLogs() = logsRef
        .where { "userId" equalTo  userId() }
        .orderBy("creationTime", Direction.DESCENDING)
        .snapshots.map {
            log.d {"getUserLogs onEach" }
            it.documents.map { document ->
                log.d { "getUserLogs $document" }
                document.data<HitchLog>()
            }
        }.map {
            Response.Success(it)
        }.flowOn(Dispatchers.IO)

    private fun <T> snapshotFlow(body:suspend () -> Flow<T>): Flow<Response<T>> =
        flow {
            body()
                .catch { error ->
                    error.message?.let { errorMessage ->
                        log.e(err = error) { errorMessage }
                        emit(Response.Failure(errorMessage))
                    }
            }.map { result ->
                Response.Success(result)
            }.flowOn(Dispatchers.IO)
        }

//    override fun getLogs() = snapshotFlow {
//        logsRef
//            .where { "userId" equalTo  userId() }
//            .orderBy("creationTime", Direction.DESCENDING)
//            .snapshots
//            .map {
//                log.d {"getUserLogs onEach" }
//                it.documents.map { document ->
//                    log.d { "getUserLogs $document" }
//                    document.data<HitchLog>()
//                }
//            }
//    }

    override fun getLog(logId: String) = repositoryFlow {
        with (logsRef.document(logId).get()) {
            when {
                !exists -> throw Exception("Document $logId doesn't exist")
                else -> return@repositoryFlow (data<HitchLog>()).also {
                    log.d { "log doc ${data<HitchLog>()}" }
                }
            }
        }
    }

    override fun addLog(log: HitchLog) = repositoryFlow {
        val id = Uuid.random().toString()
        logsRef.document(id).set(log.copy(id = id))
    }

    override fun updateLog(log: HitchLog) = repositoryFlow {
        logsRef.document(log.id).set(log)
    }

    override fun deleteLog(id: String) = repositoryFlow {
        logsRef.document(id).delete()
    }

    override fun getLogRecords(logId: String) =
        logRecordsRef(logId).orderBy("timestamp").snapshots().map {
            it.documents.map {document ->
                document.data<FirestoreHitchLogRecord>().toHitchLogRecord().also {
                    log.d { "getLogRecords $it" }
                }
            }
        }.map {
            Response.Success(it)
        }

    override fun getRecord(logId: String, recordId: String) = repositoryFlow {
        with(logRecordsRef(logId).document(recordId).get()) {
            when {
                !exists -> throw Exception("Document $recordId doesn't exist")
                else -> data<FirestoreHitchLogRecord>().toHitchLogRecord().let {
                    log.d { "getRecord $it"}
                    return@repositoryFlow it
                }
            }
        }
    }

    override fun addRecord(logId: String, record: HitchLogRecord) = repositoryFlow {
        val id = Uuid.random().toString()

        val firestoreRecord = FirestoreHitchLogRecord(
            record,
            id,
            getNextTime(logId, record.time.toTimestamp())
        )

        logRecordsRef(logId).document(id).set(firestoreRecord)
    }

    override fun updateRecord(logId: String, record: HitchLogRecord) = repositoryFlow {
        val existing = logRecordsRef(logId).document(record.id).get().data<FirestoreHitchLogRecord>()

        val updatedRecord = if (existing.timestamp == record.time.toTimestamp()) {
            FirestoreHitchLogRecord(record)
        } else {
            FirestoreHitchLogRecord(
                from = record,
                timestamp = getNextTime(logId, record.time.toTimestamp())
            )
        }

        logRecordsRef(logId).document(updatedRecord.id).set(updatedRecord)
    }

    override fun deleteRecord(logId: String, record: HitchLogRecord) = repositoryFlow {
        logRecordsRef(logId).document(record.id).delete()
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
