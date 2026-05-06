package org.gmautostop.hitchlogmp.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
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

@OptIn(ExperimentalUuidApi::class, ExperimentalCoroutinesApi::class)
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
        }.flowOn(Dispatchers.Default)

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

    /**
     * Wraps a Firestore snapshot query with authentication check.
     * Automatically cancels the listener when user logs out.
     * 
     * @param onUnauthenticated Flow to emit when user is not authenticated
     * @param errorMessage Error message prefix for logging
     * @param snapshotQuery Lambda that creates the Firestore snapshot flow when authenticated
     */
    private fun <T> authenticatedSnapshot(
        errorMessage: String,
        onUnauthenticated: () -> Flow<T>,
        snapshotQuery: (userId: String) -> Flow<T>
    ): Flow<Response<T>> {
        return authService.currentUser
            .map { user -> user?.id }
            .flatMapLatest { userId ->
                if (userId == null) {
                    onUnauthenticated()
                } else {
                    snapshotQuery(userId)
                }
            }
            .map<T, Response<T>> { Response.Success(it) }
            .catch { error ->
                log.e(err = error) { "$errorMessage: ${error.message}" }
                val appError = when (error) {
                    is NotAuthenticatedException -> AppError.NotAuthenticated
                    is NotFoundException -> AppError.NotFound
                    else -> AppError.NetworkError(error.message ?: errorMessage)
                }
                emit(Response.Failure(appError))
            }
            .flowOn(Dispatchers.Default)
    }

    /**
     * Wraps a Firestore snapshot query with authentication check.
     * Returns empty value when user logs out.
     */
    private fun <T> authenticatedSnapshot(
        emptyValue: T,
        errorMessage: String,
        snapshotQuery: (userId: String) -> Flow<T>
    ): Flow<Response<T>> = authenticatedSnapshot(
        errorMessage = errorMessage,
        onUnauthenticated = { flow { emit(emptyValue) } },
        snapshotQuery = snapshotQuery
    )

    /**
     * Wraps a Firestore snapshot query with authentication check.
     * Throws NotAuthenticatedException when user logs out.
     */
    private fun <T> authenticatedSnapshot(
        errorMessage: String,
        snapshotQuery: (userId: String) -> Flow<T>
    ): Flow<Response<T>> = authenticatedSnapshot(
        errorMessage = errorMessage,
        onUnauthenticated = { flow { throw NotAuthenticatedException() } },
        snapshotQuery = snapshotQuery
    )

    /**
     * Checks if user is authenticated and returns userId.
     * Throws NotAuthenticatedException if not authenticated.
     */
    private fun requireAuth(): String {
        return authService.currentUser.value?.id
            ?: throw NotAuthenticatedException()
    }

    override fun getLogs() = authenticatedSnapshot(
        emptyValue = emptyList<HitchLog>(),
        errorMessage = "getLogs error"
    ) { userId ->
        logsRef
            .where { "userId" equalTo userId }
            .orderBy("creationTime", Direction.DESCENDING)
            .snapshots.map { snapshot ->
                snapshot.documents.map { document ->
                    document.data<HitchLog>()
                }
            }
    }


    override fun getLog(logId: String): Flow<Response<HitchLog>> = 
        authenticatedSnapshot(
            errorMessage = "getLog error"
        ) { userId ->
            logsRef.document(logId).snapshots
                .map { snapshot ->
                    if (!snapshot.exists) throw NotFoundException(logId)
                    snapshot.data<HitchLog>()
                }
        }

    override fun addLog(log: HitchLog) = repositoryFlow(isWrite = true) {
        val userId = requireAuth()
        val id = Uuid.random().toString()
        firestoreWrite("addLog") {
            logsRef.document(id).set(log.copy(id = id, userId = userId))
        }
    }

    override fun updateLog(log: HitchLog) = repositoryFlow(isWrite = true) {
        requireAuth()
        firestoreWrite("updateLog") {
            logsRef.document(log.id).set(log)
        }
    }

    override fun deleteLog(id: String) = repositoryFlow(isWrite = true) {
        requireAuth()
        firestoreWrite("deleteLog") {
            logsRef.document(id).delete()
        }
    }

    override fun getLogRecords(logId: String) = 
        authenticatedSnapshot(
            emptyValue = emptyList(),
            errorMessage = "getLogRecords error"
        ) { userId ->
            logRecordsRef(logId).orderBy("timestamp").snapshots().map { snapshot ->
                snapshot.documents.map { document ->
                    document.data<FirestoreHitchLogRecord>().toHitchLogRecord()
                }
            }
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
        requireAuth()
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
        requireAuth()
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
        requireAuth()
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
