package org.gmautostop.hitchlogmp.ui.export

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.gmautostop.hitchlogmp.dateFormat
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.export.ChronicleFormatter
import org.gmautostop.hitchlogmp.timeFormatForDisplay

class CsvChronicleFormatter : ChronicleFormatter {
    
    private val moscowTZ = TimeZone.of("Europe/Moscow")
    
    private fun toMoscow(dt: LocalDateTime): LocalDateTime =
        dt.toInstant(TimeZone.currentSystemDefault()).toLocalDateTime(moscowTZ)
    
    override suspend fun format(
        log: HitchLog,
        records: List<HitchLogRecord>,
        history: List<Pair<String, HitchLogRecordHistoryEntry>>?,
        exportStrings: ExportStrings
    ): ByteArray {
        val csv = buildString {
            append("date,time,type,note\n")

            val sorted = records
                .map { it.copy(time = toMoscow(it.time)) }
                .sortedBy { it.time }

            for (record in sorted) {
                val dateStr = dateFormat.format(record.time.date)
                val timeStr = timeFormatForDisplay.format(record.time)
                val typeStr = record.type.name
                val noteStr = escapeCsv(record.text)

                append(dateStr)
                append(",")
                append(timeStr)
                append(",")
                append(typeStr)
                append(",")
                append(noteStr)
                append("\n")
            }
        }
        
        // Add BOM for Excel compatibility
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        return bom + csv.encodeToByteArray()
    }
    
    private fun escapeCsv(value: String): String {
        if (value.isEmpty()) return value
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            return "\"${value.replace("\"", "\"\"")}\""
        }
        return value
    }
}
