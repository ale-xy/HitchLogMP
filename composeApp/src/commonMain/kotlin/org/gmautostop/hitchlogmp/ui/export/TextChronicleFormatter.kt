package org.gmautostop.hitchlogmp.ui.export

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.gmautostop.hitchlogmp.domain.logic.computeRestDivisions
import org.gmautostop.hitchlogmp.domain.logic.computeRestMinutes
import org.gmautostop.hitchlogmp.domain.logic.formatMinutes
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.export.ChronicleFormatter
import org.gmautostop.hitchlogmp.formatDateLocale
import org.gmautostop.hitchlogmp.timeFormatForDisplay

class TextChronicleFormatter : ChronicleFormatter {
    
    private val moscowTZ = TimeZone.of("Europe/Moscow")
    
    private fun toMoscow(dt: LocalDateTime): LocalDateTime =
        dt.toInstant(TimeZone.currentSystemDefault()).toLocalDateTime(moscowTZ)
    
    override suspend fun format(
        log: HitchLog,
        records: List<HitchLogRecord>,
        history: List<Pair<String, HitchLogRecordHistoryEntry>>?,
        exportStrings: ExportStrings
    ): ByteArray {
        val text = buildString {
            append(log.name)
            append("\n\n")

            val recordsByDate = records
                .map { it.copy(time = toMoscow(it.time)) }
                .sortedBy { it.time }
                .groupBy { it.time.date }

            for ((date, dayRecords) in recordsByDate) {
                append(formatDateLocale(date))
                append("\n\n")

                for (record in dayRecords) {
                    val timeStr = timeFormatForDisplay.format(record.time)
                    val typeLabel = exportStrings.recordTypeLabels[record.type]!!
                    append(timeStr)
                    append(" ")
                    append(typeLabel)
                    if (record.text.isNotBlank()) {
                        append(" — ")
                        append(record.text)
                    }
                    append("\n")
                }
                append("\n")
            }

            append("——————\n")
            val lifts = records.count { it.type == HitchLogRecordType.LIFT }
            val checkpoints = records.count { it.type == HitchLogRecordType.CHECKPOINT }
            val restMin = computeRestMinutes(records)
            val restDivisions = computeRestDivisions(records)

            append("${exportStrings.liftsLabel}: $lifts\n")
            append("${exportStrings.checkpointsLabel}: $checkpoints\n")
            append("${exportStrings.restUsedFull}: ${formatMinutes(restMin)}/$restDivisions\n")
        }
        
        return text.encodeToByteArray()
    }
}
