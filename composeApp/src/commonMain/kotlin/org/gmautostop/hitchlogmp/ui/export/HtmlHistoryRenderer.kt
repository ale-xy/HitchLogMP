package org.gmautostop.hitchlogmp.ui.export

import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.tr
import kotlinx.html.unsafe
import org.gmautostop.hitchlogmp.dateFormat
import org.gmautostop.hitchlogmp.domain.history.buildLogHistoryData
import org.gmautostop.hitchlogmp.domain.model.ChangeType
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.model.RecordFields
import org.gmautostop.hitchlogmp.formatDateLocale
import org.gmautostop.hitchlogmp.timeFormatForDisplay

object HtmlHistoryRenderer {
    
    suspend fun renderHistory(
        history: List<Pair<String, HitchLogRecordHistoryEntry>>,
        exportStrings: ExportStrings
    ): String {
        val historyData = buildLogHistoryData(
            rawData = history,
            formatDate = { formatDateLocale(it) },
            formatTime = { timeFormatForDisplay.format(it) },
            formatEditedAt = { dateFormat.format(it.date) + " " + timeFormatForDisplay.format(it) },
            useNumericDateFormat = true
        )
        
        val recordTypeLabels = historyData.byRecordGroups.associate { group ->
            group.liveType to exportStrings.recordTypeLabels[group.liveType]!!
        }
        
        val timeTypeLabels = historyData.byTimeGroups.flatMap { group ->
            group.entries.map { it.recordType }
        }.toSet().associate { type ->
            type to exportStrings.recordTypeLabels[type]!!
        }
        
        val changeTypeLabels = (historyData.byRecordGroups.flatMap { group ->
            group.entries.map { it.changeType }
        } + historyData.byTimeGroups.flatMap { group ->
            group.entries.map { it.changeType }
        }).toSet().associate { type ->
            type to exportStrings.changeTypeLabels[type]!!
        }
        
        val recordGroupsHtml = buildString {
            appendHTML().table {
                thead {
                    tr {
                        th { text(exportStrings.columnAction) }
                        th { text(exportStrings.columnEditDateTime) }
                        th { text(exportStrings.columnBefore) }
                        th { text(exportStrings.columnAfter) }
                    }
                }
                tbody {
                    for (group in historyData.byRecordGroups) {
                        val headerClass = if (group.isDeleted) "record-header deleted" else "record-header"
                        val typeLabelResolved = recordTypeLabels[group.liveType] ?: ""
                        val timeStr = group.formattedOriginalTime ?: "—"
                        val textPreview = if (group.liveText.length > 30) {
                            group.liveText.take(30) + "..."
                        } else {
                            group.liveText
                        }
                        
                        tr(headerClass) {
                            td {
                                colSpan = "4"
                                text(typeLabelResolved)
                                text(" · ")
                                text(timeStr)
                                if (textPreview.isNotBlank()) {
                                    text(" — ")
                                    text(textPreview)
                                }
                            }
                        }
                        
                        for (entry in group.entries) {
                            val bgColor = getChangeTypeColor(entry.changeType)
                            val actionLabel = changeTypeLabels[entry.changeType] ?: ""
                            
                            val beforeHtml = when (entry.changeType) {
                                ChangeType.CREATE -> ""
                                ChangeType.DELETE -> renderFullRecordState(
                                    entry.before, exportStrings, recordTypeLabels
                                )
                                ChangeType.UPDATE -> renderChangedFieldsBefore(
                                    entry.before, entry.after, exportStrings, recordTypeLabels
                                )
                            }
                            
                            val afterHtml = when (entry.changeType) {
                                ChangeType.CREATE -> renderFullRecordState(
                                    entry.after, exportStrings, recordTypeLabels
                                )
                                ChangeType.DELETE -> "<span style=\"color: #BA1A1A\">${exportStrings.recordDeleted}</span>"
                                ChangeType.UPDATE -> renderChangedFieldsAfter(
                                    entry.before, entry.after, exportStrings, recordTypeLabels
                                )
                            }
                            
                            tr {
                                style = "background-color: $bgColor"
                                td { text(actionLabel) }
                                td { text(entry.formattedEditedAt) }
                                td { unsafe { +beforeHtml } }
                                td { unsafe { +afterHtml } }
                            }
                        }
                    }
                }
            }
        }
        
        val timeGroupsHtml = buildString {
            appendHTML().table {
                thead {
                    tr {
                        th { text(exportStrings.columnRecord) }
                        th { text(exportStrings.columnAction) }
                        th { text(exportStrings.columnEditTime) }
                        th { text(exportStrings.columnBefore) }
                        th { text(exportStrings.columnAfter) }
                    }
                }
                tbody {
                    for (group in historyData.byTimeGroups) {
                        tr("date-header") {
                            td {
                                colSpan = "5"
                                text(group.dateLabel)
                            }
                        }
                        
                        for (entry in group.entries) {
                            val bgColor = getChangeTypeColor(entry.changeType)
                            val typeLabelResolved = timeTypeLabels[entry.recordType] ?: ""
                            val actionLabel = changeTypeLabels[entry.changeType] ?: ""
                            
                            val beforeHtml = when (entry.changeType) {
                                ChangeType.CREATE -> ""
                                ChangeType.DELETE, ChangeType.UPDATE -> renderFullRecordState(
                                    entry.before, exportStrings, recordTypeLabels
                                )
                            }
                            
                            val afterHtml = when (entry.changeType) {
                                ChangeType.CREATE -> renderFullRecordState(
                                    entry.after, exportStrings, recordTypeLabels
                                )
                                ChangeType.DELETE -> "<span style=\"color: #BA1A1A\">${exportStrings.recordDeleted}</span>"
                                ChangeType.UPDATE -> renderFullRecordState(
                                    entry.after, exportStrings, recordTypeLabels
                                )
                            }
                            
                            tr {
                                style = "background-color: $bgColor"
                                td { text(typeLabelResolved) }
                                td { text(actionLabel) }
                                td { text(entry.formattedEditedAt) }
                                td { unsafe { +beforeHtml } }
                                td { unsafe { +afterHtml } }
                            }
                        }
                    }
                }
            }
        }
        
        return buildString {
            appendHTML().html {
                head {
                    style {
                        unsafe {
                            +"""
                                body { font-family: Arial, sans-serif; margin: 20px; }
                                table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }
                                th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
                                th { background-color: #f0f0f0; font-weight: bold; }
                                .date-header { background-color: #e0e0e0; font-weight: bold; }
                                .record-header { background-color: #e8eaf6; font-weight: bold; font-size: 14px; }
                                .record-header.deleted { background-color: #FFDAD6; text-decoration: line-through; }
                                del { color: #BA1A1A; text-decoration: line-through; }
                            """.trimIndent()
                        }
                    }
                }
                body {
                    h2 { text("История правок") }
                    
                    h3 { text(exportStrings.sortByRecord) }
                    unsafe { +recordGroupsHtml }
                    
                    h3 { text(exportStrings.sortByTime) }
                    unsafe { +timeGroupsHtml }
                }
            }
        }
    }
    
    // Render full record state (for "By Time" sheet and DELETE in "By Records")
    private fun renderFullRecordState(
        fields: RecordFields?,
        exportStrings: ExportStrings,
        recordTypeLabels: Map<org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType, String>
    ): String {
        if (fields == null) return ""
        
        return buildString {
            appendHTML().div {
                p {
                    text("${exportStrings.timeLabel}: ${fields.formattedTime}")
                }
                p {
                    text("${exportStrings.typeLabel}: ${recordTypeLabels[fields.type] ?: ""}")
                }
                if (fields.text != null) {
                    p {
                        text("${exportStrings.textLabel}: ${fields.text}")
                    }
                }
            }
        }
    }
    
    // Render only changed fields - BEFORE values (for "By Records" sheet UPDATE)
    private fun renderChangedFieldsBefore(
        before: RecordFields?,
        after: RecordFields?,
        exportStrings: ExportStrings,
        recordTypeLabels: Map<org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType, String>
    ): String {
        if (before == null || after == null) return ""
        
        return buildString {
            appendHTML().div {
                // Check time change
                if (before.time != after.time) {
                    p {
                        text("${exportStrings.timeLabel}: ")
                        unsafe { +"<del>${before.formattedTime}</del>" }
                    }
                }
                
                // Check type change
                if (before.type != after.type) {
                    p {
                        text("${exportStrings.typeLabel}: ")
                        unsafe { +"<del>${recordTypeLabels[before.type] ?: ""}</del>" }
                    }
                }
                
                // Check text change
                if (before.text != after.text && before.text != null) {
                    p {
                        text("${exportStrings.textLabel}: ")
                        unsafe { +"<del>${before.text}</del>" }
                    }
                }
            }
        }
    }
    
    // Render only changed fields - AFTER values (for "By Records" sheet UPDATE)
    private fun renderChangedFieldsAfter(
        before: RecordFields?,
        after: RecordFields?,
        exportStrings: ExportStrings,
        recordTypeLabels: Map<org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType, String>
    ): String {
        if (before == null || after == null) return ""
        
        return buildString {
            appendHTML().div {
                // Check time change
                if (before.time != after.time) {
                    p {
                        text("${exportStrings.timeLabel}: ${after.formattedTime}")
                    }
                }
                
                // Check type change
                if (before.type != after.type) {
                    p {
                        text("${exportStrings.typeLabel}: ${recordTypeLabels[after.type] ?: ""}")
                    }
                }
                
                // Check text change
                if (before.text != after.text && after.text != null) {
                    p {
                        text("${exportStrings.textLabel}: ${after.text}")
                    }
                }
            }
        }
    }
    
    private fun getChangeTypeColor(changeType: ChangeType): String = when (changeType) {
        ChangeType.CREATE -> "#E8F5E9"
        ChangeType.UPDATE -> "#FFF8E1"
        ChangeType.DELETE -> "#FFEBEE"
    }
}
