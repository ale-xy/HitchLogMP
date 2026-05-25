package org.gmautostop.hitchlogmp.ui.export

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.gmautostop.hitchlogmp.dateFormat
import org.gmautostop.hitchlogmp.domain.ExcelColumn
import org.gmautostop.hitchlogmp.domain.generateXlsxBytes
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.export.ChronicleFormatter
import org.gmautostop.hitchlogmp.timeFormatForDisplay

/**
 * DTO for XLSX export with column headers defined via @ExcelColumn annotations.
 */
@Serializable
data class HitchLogRecordExportRow(
    @ExcelColumn("Дата") val date: String,
    @ExcelColumn("Время") val time: String,
    @ExcelColumn("Тип") val type: String,
    @ExcelColumn("Примечание") val note: String
)

class XlsxChronicleFormatter : ChronicleFormatter {
    
    private val moscowTZ = TimeZone.of("Europe/Moscow")
    
    private fun toMoscow(dt: LocalDateTime): LocalDateTime =
        dt.toInstant(TimeZone.currentSystemDefault()).toLocalDateTime(moscowTZ)
    
    override suspend fun format(
        log: HitchLog,
        records: List<HitchLogRecord>,
        history: List<Pair<String, HitchLogRecordHistoryEntry>>?,
        exportStrings: ExportStrings
    ): ByteArray {
        val rows = records
            .map { it.copy(time = toMoscow(it.time)) }
            .sortedBy { it.time }
            .map { record ->
                HitchLogRecordExportRow(
                    date = dateFormat.format(record.time.date),
                    time = timeFormatForDisplay.format(record.time),
                    type = exportStrings.recordTypeLabels[record.type]!!,
                    note = record.text
                )
            }
        
        return generateXlsxBytes(rows)
    }
}
