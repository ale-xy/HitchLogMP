package org.gmautostop.hitchlogmp.domain.history

import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.domain.model.ChangeType
import org.gmautostop.hitchlogmp.domain.model.DateGroup
import org.gmautostop.hitchlogmp.domain.model.HistoryData
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.model.LogHistoryEntry
import org.gmautostop.hitchlogmp.domain.model.RecordFields
import org.gmautostop.hitchlogmp.domain.model.RecordGroup
import org.gmautostop.hitchlogmp.domain.model.RecordVersion
import org.gmautostop.hitchlogmp.localTZDateTime

/**
 * Builds structured history data from raw Firestore history entries.
 * 
 * @param rawData List of (recordId, historyEntry) pairs from repository
 * @param formatDate Function to format LocalDate to string (e.g., "16 МАЯ 2026")
 * @param formatTime Function to format LocalDateTime to string (e.g., "09:30")
 * @param formatEditedAt Function to format edited timestamp (e.g., "16.05.2026 09:32")
 * @param useNumericDateFormat If true, uses numeric format for dates (for export)
 * @return HistoryData with grouped entries by time and by record
 */
fun buildLogHistoryData(
    rawData: List<Pair<String, HitchLogRecordHistoryEntry>>,
    formatDate: (kotlinx.datetime.LocalDate) -> String,
    formatTime: (LocalDateTime) -> String,
    formatEditedAt: (LocalDateTime) -> String,
    useNumericDateFormat: Boolean = false
): HistoryData {
    val grouped: Map<String, List<HitchLogRecordHistoryEntry>> = rawData
        .groupBy { (recordId, _) -> recordId }
        .mapValues { (_, pairs) -> pairs.map { it.second } }

    val byRecordGroups = grouped.map { (recordId, entries) ->
        val sorted = entries.sortedBy { it.editedAt }
        val liveEntry = sorted.last()
        val liveType = liveEntry.type
        val liveText = liveEntry.text
        val isDeleted = liveEntry.changeType == ChangeType.DELETE
        val originalTime = sorted.firstOrNull { it.changeType == ChangeType.CREATE }?.time

        val versions = computeRecordVersions(sorted, formatTime, formatEditedAt)
        val logHistoryEntries = versions.map { version ->
            LogHistoryEntry(
                historyId = version.historyId,
                editedAt = version.editedAt,
                formattedEditedAt = version.formattedEditedAt,
                recordId = recordId,
                changeType = version.changeType,
                recordType = liveType,
                before = version.before,
                after = version.after
            )
        }

        RecordGroup(
            recordId = recordId,
            liveType = liveType,
            formattedOriginalTime = originalTime?.let { formatTime(it) },
            liveText = liveText,
            isDeleted = isDeleted,
            entries = logHistoryEntries
        )
    }.sortedBy { group ->
        grouped[group.recordId]?.minByOrNull { it.editedAt }?.editedAt
    }

    val allEntries = byRecordGroups.flatMap { it.entries }.sortedBy { it.editedAt }

    val byTimeGroups = allEntries
        .groupBy { it.editedAt.localTZDateTime().date }
        .entries
        .sortedBy { it.key }
        .map { (date, entries) ->
            DateGroup(
                dateLabel = formatDate(date),
                date = date,
                entries = entries
            )
        }

    return HistoryData(
        byTimeGroups = byTimeGroups,
        byRecordGroups = byRecordGroups
    )
}

/**
 * Converts a sorted-ascending list of HitchLogRecordHistoryEntry into
 * RecordVersion items with before/after RecordFields snapshots.
 *
 * The repository stores each entry as follows:
 *   CREATE → stored = AFTER state; before=null, after=snapshot
 *   UPDATE → stored = AFTER state; before=prevSnapshot, after=snapshot
 *   DELETE → stored data ignored; before=prevSnapshot (record just before deletion), after=null
 *
 * @param entries List of history entries for a single record, sorted by editedAt
 * @param formatTime Function to format record time (e.g., "09:30")
 * @param formatEditedAt Function to format edited timestamp
 */
fun computeRecordVersions(
    entries: List<HitchLogRecordHistoryEntry>,
    formatTime: (LocalDateTime) -> String,
    formatEditedAt: (LocalDateTime) -> String
): List<RecordVersion> {
    val sorted = entries.sortedBy { it.editedAt }
    return sorted.mapIndexed { i, entry ->
        val prev = if (i > 0) sorted[i - 1] else null
        fun fields(e: HitchLogRecordHistoryEntry) = RecordFields(
            time = e.time,
            formattedTime = formatTime(e.time),
            type = e.type,
            text = e.text.takeIf { it.isNotEmpty() }
        )
        val snapshot = fields(entry)
        val prevSnapshot = prev?.let { fields(it) }
        val formattedEditedAtStr = formatEditedAt(entry.editedAt.localTZDateTime())
        when (entry.changeType) {
            ChangeType.CREATE -> RecordVersion(
                entry.historyId, entry.editedAt, formattedEditedAtStr, entry.changeType,
                before = null, after = snapshot
            )
            ChangeType.UPDATE -> RecordVersion(
                entry.historyId, entry.editedAt, formattedEditedAtStr, entry.changeType,
                before = prevSnapshot, after = snapshot
            )
            ChangeType.DELETE -> RecordVersion(
                entry.historyId, entry.editedAt, formattedEditedAtStr, entry.changeType,
                before = prevSnapshot, after = null
            )
        }
    }
}
