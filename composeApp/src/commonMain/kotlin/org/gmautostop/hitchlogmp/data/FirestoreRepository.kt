package org.gmautostop.hitchlogmp.data

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.firestore.fromMilliseconds
import dev.gitlive.firebase.firestore.toMilliseconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.toLocalDateTime
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
        logRecordsRef(logId).orderBy("time").snapshots().map {
            it.documents.map {document ->
                document.data<HitchLogRecord>()
            }
        }.map {
            Response.Success(it)
        }


    override fun getRecord(logId: String, recordId: String) = repositoryFlow {
        with(logRecordsRef(logId).document(recordId).get()) {
            when {
                !exists -> throw Exception("Document $recordId doesn't exist")
                else -> return@repositoryFlow (data<HitchLogRecord>()).also {
                    log.d { "log record ${data<HitchLogRecord>()}"}
                }
            }
        }
    }

    override fun addRecord(logId: String, record: HitchLogRecord) = repositoryFlow {
        val id = Uuid.random().toString()
        logRecordsRef(logId).document(id)
            .set(record.copy(
                id = id,
                time = getNextTime(logId, record.time)
            ))
    }

    override fun updateRecord(logId: String, record: HitchLogRecord) = repositoryFlow {
        val existing = logRecordsRef(logId).document(record.id).get().data<HitchLogRecord>()

        val updatedRecord = if (existing.time == record.time) {
            record
        } else {
            record.copy(time = getNextTime(logId, record.time))
        }

        logRecordsRef(logId).document(record.id).set(updatedRecord)
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
                    .where { "time" greaterThanOrEqualTo enteredTime }
                    .where { "time" lessThan enteredTime.toLocalDateTime().addMinute().toTimestamp() }
                    .get(Source.CACHE)
                    .documents.map { it.data<HitchLogRecord>() }
                    .maxByOrNull { it.time.seconds }?.time?.toLocalDateTime()?.addSecond()?.toTimestamp()
            ?: enteredTime
    }

    private fun LocalDateTime.addSecond(): LocalDateTime =
        toInstant(TimeZone.currentSystemDefault()).plus(1, DateTimeUnit.SECOND).localTZDateTime()
    private fun LocalDateTime.addMinute(): LocalDateTime =
        toInstant(TimeZone.currentSystemDefault()).plus(1, DateTimeUnit.MINUTE).localTZDateTime()


    companion object {
        val log = logging()
    }
}
