package org.gmautostop.hitchlogmp.ui.export

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.unsafe
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

class HtmlChronicleFormatter : ChronicleFormatter {
    
    private val moscowTZ = TimeZone.of("Europe/Moscow")
    
    private fun toMoscow(dt: LocalDateTime): LocalDateTime =
        dt.toInstant(TimeZone.currentSystemDefault()).toLocalDateTime(moscowTZ)
    
    override suspend fun format(
        log: HitchLog,
        records: List<HitchLogRecord>,
        history: List<Pair<String, HitchLogRecordHistoryEntry>>?,
        exportStrings: ExportStrings
    ): ByteArray {
        val lifts = records.count { it.type == HitchLogRecordType.LIFT }
        val checkpoints = records.count { it.type == HitchLogRecordType.CHECKPOINT }
        val restMin = computeRestMinutes(records)
        val restDivisions = computeRestDivisions(records)

        val sorted = records
            .map { it.copy(time = toMoscow(it.time)) }
            .sortedBy { it.time }

        val recordsByDate = sorted.groupBy { it.time.date }
        
        val typeLabels = sorted.associate { record ->
            record.type to exportStrings.recordTypeLabels[record.type]!!
        }
        
        val historyHtml = if (history != null && history.isNotEmpty()) {
            HtmlHistoryRenderer.renderHistory(history, exportStrings)
        } else {
            null
        }

        val html = buildString {
            appendHTML().html {
                head {
                    meta(charset = "UTF-8")
                    title(log.name)
                    style {
                        unsafe {
                            +"""
                                body { font-family: Arial, sans-serif; margin: 20px; }
                                table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }
                                th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
                                th { background-color: #f0f0f0; font-weight: bold; }
                                .summary-table { max-width: 500px; }
                                .date-header { background-color: #e0e0e0; font-weight: bold; }
                                .record-header { background-color: #e8eaf6; font-weight: bold; font-size: 14px; }
                                .record-header.deleted { background-color: #FFDAD6; text-decoration: line-through; }
                                del { color: #BA1A1A; text-decoration: line-through; }
                            """.trimIndent()
                        }
                    }
                }
                body {
                    h1 { text(log.name) }
                    
                    h2 { text(exportStrings.chronicle) }
                    table {
                        thead {
                            tr {
                                th { text(exportStrings.timeLabel) }
                                th { text(exportStrings.typeLabel) }
                                th { text(exportStrings.noteLabel) }
                            }
                        }
                        tbody {
                            for ((date, dayRecords) in recordsByDate) {
                                val dateStr = formatDateLocale(date)
                                tr("date-header") {
                                    td {
                                        colSpan = "3"
                                        text(dateStr)
                                    }
                                }
                                for (record in dayRecords) {
                                    val timeStr = timeFormatForDisplay.format(record.time)
                                    val typeLabel = typeLabels[record.type] ?: record.type.name
                                    val color = getRowColor(record.type)
                                    tr {
                                        style = "background-color: $color;"
                                        td { text(timeStr) }
                                        td { text(typeLabel) }
                                        td { text(record.text) }
                                    }
                                }
                            }
                        }
                    }
                    
                    h2 { text(exportStrings.summary) }
                    table("summary-table") {
                        tr {
                            td { text(exportStrings.recordsLabel) }
                            td { text(records.size.toString()) }
                        }
                        tr {
                            td { text(exportStrings.liftsLabel) }
                            td { text(lifts.toString()) }
                        }
                        tr {
                            td { text(exportStrings.checkpointsLabel) }
                            td { text(checkpoints.toString()) }
                        }
                        tr {
                            td { text(exportStrings.restUsedFull) }
                            td { text("${formatMinutes(restMin)}/$restDivisions") }
                        }
                    }
                    
                    if (historyHtml != null) {
                        unsafe {
                            +historyHtml
                        }
                    }
                }
            }
        }
        
        return html.encodeToByteArray()
    }
    
    private fun getRowColor(type: HitchLogRecordType): String = when (type) {
        HitchLogRecordType.START, HitchLogRecordType.FINISH -> "#FCE4EC"
        HitchLogRecordType.LIFT -> "#E3F2FD"
        HitchLogRecordType.GET_OFF -> "#F3E5F5"
        HitchLogRecordType.WALK, HitchLogRecordType.WALK_END -> "#FFF8E1"
        HitchLogRecordType.CHECKPOINT -> "#E8F5E9"
        HitchLogRecordType.REST_ON, HitchLogRecordType.REST_OFF -> "#FBE9E7"
        HitchLogRecordType.OFFSIDE_ON, HitchLogRecordType.OFFSIDE_OFF -> "#ECEFF1"
        HitchLogRecordType.RETIRE -> "#FFEBEE"
        HitchLogRecordType.MEET, HitchLogRecordType.FREE_TEXT -> "#FFFFFF"
    }
    
    private suspend fun renderHistory(history: List<Pair<String, HitchLogRecordHistoryEntry>>, exportStrings: ExportStrings): String {
        return HtmlHistoryRenderer.renderHistory(history, exportStrings)
    }
}
