package org.gmautostop.hitchlogmp.ui.export

import org.gmautostop.hitchlogmp.dateFormat
import org.gmautostop.hitchlogmp.domain.history.buildLogHistoryData
import org.gmautostop.hitchlogmp.domain.model.ChangeType
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.model.RecordFields
import org.gmautostop.hitchlogmp.formatDateLocale
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.export.xlsx.XlsxSheetBuilder

object XlsxHistoryFormatter {
    
    fun addHistorySheets(
        builder: org.gmautostop.hitchlogmp.ui.export.xlsx.XlsxWorkbookBuilder,
        history: List<Pair<String, HitchLogRecordHistoryEntry>>,
        exportStrings: ExportStrings
    ) {
        val historyData = buildLogHistoryData(
            rawData = history,
            formatDate = { formatDateLocale(it) },
            formatTime = { timeFormatForDisplay.format(it) },
            formatEditedAt = { dateFormat.format(it.date) + " " + timeFormatForDisplay.format(it) },
            useNumericDateFormat = true
        )
        
        // Build label maps
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
        
        // Add "By Records" sheet
        builder.sheet(exportStrings.sortByRecord) {
            formatHistoryByRecord(historyData, recordTypeLabels, changeTypeLabels, exportStrings)
        }
        
        // Add "By Time" sheet
        builder.sheet(exportStrings.sortByTime) {
            formatHistoryByTime(historyData, timeTypeLabels, changeTypeLabels, exportStrings)
        }
    }
    
    private fun XlsxSheetBuilder.formatHistoryByRecord(
        historyData: org.gmautostop.hitchlogmp.domain.model.HistoryData,
        recordTypeLabels: Map<org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType, String>,
        changeTypeLabels: Map<ChangeType, String>,
        exportStrings: ExportStrings
    ) {
        // Header row - 4 columns
        headerRow(
            exportStrings.columnAction,
            exportStrings.columnEditDateTime,
            exportStrings.columnBefore,
            exportStrings.columnAfter
        )
        
        // Data rows
        for (group in historyData.byRecordGroups) {
            val typeLabelResolved = recordTypeLabels[group.liveType] ?: ""
            val timeStr = group.formattedOriginalTime ?: "—"
            val textPreview = if (group.liveText.length > 30) {
                group.liveText.take(30) + "..."
            } else {
                group.liveText
            }
            
            // Record header row (spans all 4 columns)
            val headerText = buildString {
                append(typeLabelResolved)
                append(" · ")
                append(timeStr)
                if (textPreview.isNotBlank()) {
                    append(" — ")
                    append(textPreview)
                }
                if (group.isDeleted) {
                    append(" [${exportStrings.recordDeleted}]")
                }
            }
            mergedRow(4, headerText, "", "", "")
            
            // Entry rows
            for (entry in group.entries) {
                val actionLabel = changeTypeLabels[entry.changeType] ?: ""
                
                val beforeText = when (entry.changeType) {
                    ChangeType.CREATE -> ""
                    ChangeType.DELETE -> formatFullRecordState(
                        entry.before, exportStrings, recordTypeLabels
                    )
                    ChangeType.UPDATE -> formatChangedFieldsBefore(
                        entry.before, entry.after, exportStrings, recordTypeLabels
                    )
                }
                
                val afterText = when (entry.changeType) {
                    ChangeType.CREATE -> formatFullRecordState(
                        entry.after, exportStrings, recordTypeLabels
                    )
                    ChangeType.DELETE -> exportStrings.recordDeleted
                    ChangeType.UPDATE -> formatChangedFieldsAfter(
                        entry.before, entry.after, exportStrings, recordTypeLabels
                    )
                }
                
                dataRow(actionLabel, entry.formattedEditedAt, beforeText, afterText)
            }
        }
    }
    
    private fun XlsxSheetBuilder.formatHistoryByTime(
        historyData: org.gmautostop.hitchlogmp.domain.model.HistoryData,
        recordTypeLabels: Map<org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType, String>,
        changeTypeLabels: Map<ChangeType, String>,
        exportStrings: ExportStrings
    ) {
        // Header row - 5 columns
        headerRow(
            exportStrings.columnRecord,
            exportStrings.columnAction,
            exportStrings.columnEditTime,
            exportStrings.columnBefore,
            exportStrings.columnAfter
        )
        
        // Data rows
        for (group in historyData.byTimeGroups) {
            // Date header row (spans all 5 columns)
            mergedRow(5, group.dateLabel, "", "", "", "")
            
            // Entry rows
            for (entry in group.entries) {
                val typeLabelResolved = recordTypeLabels[entry.recordType] ?: ""
                val actionLabel = changeTypeLabels[entry.changeType] ?: ""
                
                val beforeText = when (entry.changeType) {
                    ChangeType.CREATE -> ""
                    ChangeType.DELETE, ChangeType.UPDATE -> formatFullRecordState(
                        entry.before, exportStrings, recordTypeLabels
                    )
                }
                
                val afterText = when (entry.changeType) {
                    ChangeType.CREATE -> formatFullRecordState(
                        entry.after, exportStrings, recordTypeLabels
                    )
                    ChangeType.DELETE -> exportStrings.recordDeleted
                    ChangeType.UPDATE -> formatFullRecordState(
                        entry.after, exportStrings, recordTypeLabels
                    )
                }
                
                dataRow(
                    typeLabelResolved,
                    actionLabel,
                    entry.formattedEditedAt,
                    beforeText,
                    afterText
                )
            }
        }
    }
    
    // Format full record state (for "By Time" sheet and DELETE in "By Records")
    private fun formatFullRecordState(
        fields: RecordFields?,
        exportStrings: ExportStrings,
        recordTypeLabels: Map<org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType, String>
    ): String {
        if (fields == null) return ""
        
        return buildString {
            append(exportStrings.timeLabel)
            append(": ")
            append(fields.formattedTime)
            append("\n")
            append(exportStrings.typeLabel)
            append(": ")
            append(recordTypeLabels[fields.type] ?: "")
            if (fields.text != null) {
                append("\n")
                append(exportStrings.textLabel)
                append(": ")
                append(fields.text)
            }
        }
    }
    
    // Format only changed fields - BEFORE values (for "By Records" sheet UPDATE)
    private fun formatChangedFieldsBefore(
        before: RecordFields?,
        after: RecordFields?,
        exportStrings: ExportStrings,
        recordTypeLabels: Map<org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType, String>
    ): String {
        if (before == null || after == null) return ""
        
        val changes = mutableListOf<String>()
        
        // Check time change
        if (before.time != after.time) {
            changes.add("${exportStrings.timeLabel}: ${before.formattedTime}")
        }
        
        // Check type change
        if (before.type != after.type) {
            changes.add("${exportStrings.typeLabel}: ${recordTypeLabels[before.type] ?: ""}")
        }
        
        // Check text change
        if (before.text != after.text && before.text != null) {
            changes.add("${exportStrings.textLabel}: ${before.text}")
        }
        
        return changes.joinToString("\n")
    }
    
    // Format only changed fields - AFTER values (for "By Records" sheet UPDATE)
    private fun formatChangedFieldsAfter(
        before: RecordFields?,
        after: RecordFields?,
        exportStrings: ExportStrings,
        recordTypeLabels: Map<org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType, String>
    ): String {
        if (before == null || after == null) return ""
        
        val changes = mutableListOf<String>()
        
        // Check time change
        if (before.time != after.time) {
            changes.add("${exportStrings.timeLabel}: ${after.formattedTime}")
        }
        
        // Check type change
        if (before.type != after.type) {
            changes.add("${exportStrings.typeLabel}: ${recordTypeLabels[after.type] ?: ""}")
        }
        
        // Check text change
        if (before.text != after.text && after.text != null) {
            changes.add("${exportStrings.textLabel}: ${after.text}")
        }
        
        return changes.joinToString("\n")
    }
}
