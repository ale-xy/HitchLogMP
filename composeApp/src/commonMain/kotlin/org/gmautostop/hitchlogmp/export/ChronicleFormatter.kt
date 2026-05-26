package org.gmautostop.hitchlogmp.export

import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.ui.export.ExportStrings

/**
 * Interface for formatting chronicle data into various export formats.
 * Implementations live in the UI layer (ui/export/) as they handle presentation logic.
 */
interface ChronicleFormatter {
    /**
     * Formats a hitchhiking log with its records and optional history into bytes.
     * 
     * @param log The log metadata
     * @param records List of records in the log
     * @param history Optional list of (recordId, historyEntry) pairs for history section
     * @return Formatted data as byte array ready for file saving
     */
    suspend fun format(
        log: HitchLog,
        records: List<HitchLogRecord>,
        history: List<Pair<String, HitchLogRecordHistoryEntry>>?,
        exportStrings: ExportStrings
    ): ByteArray
}
