package org.gmautostop.hitchlogmp.ui.history

import org.gmautostop.hitchlogmp.dateTimeFormat
import org.gmautostop.hitchlogmp.domain.ChangeType
import org.gmautostop.hitchlogmp.domain.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.timeFormatForDisplay

/**
 * Converts a sorted-ascending list of HitchLogRecordHistoryEntry into
 * RecordVersionUi items with before/after RecordFields snapshots.
 *
 * The repository stores each entry as follows:
 *   CREATE → stored = AFTER state; before=null, after=snapshot
 *   UPDATE → stored = AFTER state; before=prevSnapshot, after=snapshot
 *   DELETE → stored data ignored; before=prevSnapshot (record just before deletion), after=null
 */
fun computeRecordVersions(entries: List<HitchLogRecordHistoryEntry>): List<RecordVersionUi> {
    val sorted = entries.sortedBy { it.editedAt }
    return sorted.mapIndexed { i, entry ->
        val prev = if (i > 0) sorted[i - 1] else null
        fun fields(e: HitchLogRecordHistoryEntry) = RecordFields(
            time = e.time,
            formattedTime = timeFormatForDisplay.format(e.time),
            type = e.type,
            text = e.text.takeIf { it.isNotEmpty() }
        )
        val snapshot = fields(entry)
        val prevSnapshot = prev?.let { fields(it) }
        val formattedEditedAt = dateTimeFormat.format(entry.editedAt.localTZDateTime())
        when (entry.changeType) {
            ChangeType.CREATE -> RecordVersionUi(
                entry.historyId, entry.editedAt, formattedEditedAt, entry.changeType,
                before = null, after = snapshot
            )
            ChangeType.UPDATE -> RecordVersionUi(
                entry.historyId, entry.editedAt, formattedEditedAt, entry.changeType,
                before = prevSnapshot, after = snapshot
            )
            ChangeType.DELETE -> RecordVersionUi(
                entry.historyId, entry.editedAt, formattedEditedAt, entry.changeType,
                before = prevSnapshot, after = null
            )
        }
    }
}
