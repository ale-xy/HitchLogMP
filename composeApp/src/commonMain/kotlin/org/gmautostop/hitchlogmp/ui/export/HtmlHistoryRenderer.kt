package org.gmautostop.hitchlogmp.ui.export

import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.h3
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.p
import kotlinx.html.span
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
                        th { text(exportStrings.columnChanges) }
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
                                colSpan = "3"
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
                            val diffHtml = renderInlineDiff(
                                entry.changeType, entry.before, entry.after,
                                exportStrings.recordDeleted, exportStrings.recordCreated,
                                exportStrings.timeLabel, exportStrings.textLabel, exportStrings.typeLabel,
                                recordTypeLabels, changeTypeLabels
                            )
                            
                            tr {
                                style = "background-color: $bgColor"
                                td { text(actionLabel) }
                                td { text(entry.formattedEditedAt) }
                                td { unsafe { +diffHtml } }
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
                        th { text(exportStrings.columnChanges) }
                    }
                }
                tbody {
                    for (group in historyData.byTimeGroups) {
                        tr("date-header") {
                            td {
                                colSpan = "4"
                                text(group.dateLabel)
                            }
                        }
                        
                        for (entry in group.entries) {
                            val bgColor = getChangeTypeColor(entry.changeType)
                            val typeLabelResolved = timeTypeLabels[entry.recordType] ?: ""
                            val actionLabel = changeTypeLabels[entry.changeType] ?: ""
                            val diffHtml = renderInlineDiff(
                                entry.changeType, entry.before, entry.after,
                                exportStrings.recordDeleted, exportStrings.recordCreated,
                                exportStrings.timeLabel, exportStrings.textLabel, exportStrings.typeLabel,
                                recordTypeLabels, changeTypeLabels
                            )
                            
                            tr {
                                style = "background-color: $bgColor"
                                td { text(typeLabelResolved) }
                                td { text(actionLabel) }
                                td { text(entry.formattedEditedAt) }
                                td { unsafe { +diffHtml } }
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
    
    private fun renderInlineDiff(
        changeType: ChangeType,
        before: RecordFields?,
        after: RecordFields?,
        recordDeleted: String,
        recordCreated: String,
        timeLabel: String,
        textLabel: String,
        typeLabel: String,
        recordTypeLabels: Map<org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType, String>,
        changeTypeLabels: Map<ChangeType, String>
    ): String {
        return buildString {
            appendHTML().div {
                when (changeType) {
                    ChangeType.CREATE -> {
                        if (after != null) {
                            p {
                                text(timeLabel)
                                text(": ")
                                text(after.formattedTime)
                            }
                            if (after.text != null) {
                                p {
                                    text(textLabel)
                                    text(": ")
                                    text(after.text)
                                }
                            }
                        }
                    }
                    ChangeType.DELETE -> {
                        span {
                            style = "color: #BA1A1A"
                            text(recordDeleted)
                        }
                    }
                    ChangeType.UPDATE -> {
                        val changes = mutableListOf<String>()
                        
                        if (before?.time != after?.time) {
                            val timeChange = buildString {
                                append(timeLabel)
                                append(": ")
                                if (before != null) {
                                    append("<del>")
                                    append(before.formattedTime)
                                    append("</del>")
                                    append(" → ")
                                }
                                if (after != null) {
                                    append(after.formattedTime)
                                }
                            }
                            changes.add(timeChange)
                        }
                        
                        if (before?.type != after?.type) {
                            val typeLabelBefore = before?.type?.let { recordTypeLabels[it] }
                            val typeLabelAfter = after?.type?.let { recordTypeLabels[it] }
                            val typeChange = buildString {
                                append(typeLabel)
                                append(": ")
                                if (typeLabelBefore != null) {
                                    append("<del>")
                                    append(typeLabelBefore)
                                    append("</del>")
                                    append(" → ")
                                }
                                if (typeLabelAfter != null) {
                                    append(typeLabelAfter)
                                }
                            }
                            changes.add(typeChange)
                        }
                        
                        if (before?.text != after?.text) {
                            if (before?.text != null) {
                                changes.add(
                                    textLabel + ": <del>" + before.text + "</del>"
                                )
                            }
                            if (after?.text != null) {
                                changes.add(
                                    textLabel + ": " + after.text
                                )
                            }
                        }
                        
                        if (changes.isEmpty()) {
                            text(recordCreated)
                        } else {
                            changes.joinToString("<br>").split("<br>").forEach { line ->
                                p { unsafe { +line } }
                            }
                        }
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
