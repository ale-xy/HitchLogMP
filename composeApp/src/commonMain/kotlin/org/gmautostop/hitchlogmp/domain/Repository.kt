package org.gmautostop.hitchlogmp.domain

import kotlinx.coroutines.flow.Flow
import org.gmautostop.hitchlogmp.data.HitchLog
import org.gmautostop.hitchlogmp.data.HitchLogRecord

interface Repository {
    fun userId(): String?
    fun getLogs(): Flow<Response<List<HitchLog>>>
    fun getLog(logId: String): Flow<Response<HitchLog>>
    fun addLog(log: HitchLog): Flow<Response<Unit>>
    fun updateLog(log: HitchLog): Flow<Response<Unit>>
    fun deleteLog(id: String): Flow<Response<Unit>>
    fun getLogRecords(logId: String): Flow<Response<List<HitchLogRecord>>>
    fun getRecord(logId: String, recordId: String): Flow<Response<HitchLogRecord>>
    fun addRecord(logId: String, record: HitchLogRecord): Flow<Response<Unit>>
    fun updateRecord(logId: String, record: HitchLogRecord): Flow<Response<Unit>>
    fun deleteRecord(logId: String, record: HitchLogRecord): Flow<Response<Unit>>
    fun saveRecord(logId: String, record: HitchLogRecord): Flow<Response<Unit>>
}