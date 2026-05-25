package org.gmautostop.hitchlogmp.ui.export

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.gmautostop.hitchlogmp.dateFormat
import org.gmautostop.hitchlogmp.domain.logic.computeRestDivisions
import org.gmautostop.hitchlogmp.domain.logic.computeRestMinutes
import org.gmautostop.hitchlogmp.domain.logic.formatMinutes
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.export.ChronicleFormatter
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.export.xlsx.XlsxArchiver
import org.gmautostop.hitchlogmp.ui.export.xlsx.XlsxBuilder

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
        val sortedRecords = records
            .map { it.copy(time = toMoscow(it.time)) }
            .sortedBy { it.time }
        
        val workbook = XlsxBuilder.workbook {
            // Chronicle sheet
            sheet(exportStrings.chronicle) {
                headerRow(
                    exportStrings.dateLabel,
                    exportStrings.timeLabel,
                    exportStrings.typeLabel,
                    exportStrings.noteLabel
                )
                
                sortedRecords.forEach { record ->
                    dataRow(
                        dateFormat.format(record.time.date),
                        timeFormatForDisplay.format(record.time),
                        exportStrings.recordTypeLabels[record.type]!!,
                        record.text
                    )
                }
            }
            
            // Summary sheet
            sheet(exportStrings.summary) {
                val lifts = records.count { it.type == HitchLogRecordType.LIFT }
                val checkpoints = records.count { it.type == HitchLogRecordType.CHECKPOINT }
                val restMin = computeRestMinutes(records)
                val restDivisions = computeRestDivisions(records)
                
                dataRow(exportStrings.recordsLabel, records.size)
                dataRow(exportStrings.liftsLabel, lifts)
                dataRow(exportStrings.checkpointsLabel, checkpoints)
                dataRow(exportStrings.restUsedFull, "${formatMinutes(restMin)}/$restDivisions")
            }
            
            // Add history sheets if available
            if (!history.isNullOrEmpty()) {
                XlsxHistoryFormatter.addHistorySheets(this, history, exportStrings)
            }
        }
        
        return XlsxArchiver.createXlsxBytes(workbook)
    }
}
