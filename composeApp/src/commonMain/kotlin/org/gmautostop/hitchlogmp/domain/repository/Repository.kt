package org.gmautostop.hitchlogmp.domain.repository

import kotlinx.coroutines.flow.Flow
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry

interface Repository {
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
    fun getRecordHistory(logId: String, recordId: String): Flow<Response<List<HitchLogRecordHistoryEntry>>>
    fun getLogHistory(logId: String): Flow<Response<List<Pair<String, HitchLogRecordHistoryEntry>>>>
}
