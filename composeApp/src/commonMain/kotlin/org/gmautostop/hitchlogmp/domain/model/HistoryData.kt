package org.gmautostop.hitchlogmp.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

enum class SortMode { BY_TIME, BY_RECORD }

/**
 * A snapshot of a record's editable fields at a point in time.
 * Used as before/after pairs to represent what changed in a history event.
 */
data class RecordFields(
    val time: LocalDateTime,        // kept for equality comparison in diff logic
    val formattedTime: String,
    val type: HitchLogRecordType,
    val text: String?               // null when the record has no text
)

data class RecordVersion(
    val historyId: String,
    val editedAt: Instant,          // kept for sorting
    val formattedEditedAt: String,
    val changeType: ChangeType,
    val before: RecordFields?,      // null for CREATE (no prior state)
    val after: RecordFields?        // null for DELETE (record no longer exists)
)

data class LogHistoryEntry(
    val historyId: String,
    val editedAt: Instant,          // kept for sorting
    val formattedEditedAt: String,
    val recordId: String,
    val changeType: ChangeType,
    val recordType: HitchLogRecordType,   // live (latest) type for icon chip
    val before: RecordFields?,
    val after: RecordFields?
)

data class DateGroup(
    val dateLabel: String,
    val date: LocalDate,
    val entries: List<LogHistoryEntry>
)

data class RecordGroup(
    val recordId: String,
    val liveType: HitchLogRecordType,
    val formattedOriginalTime: String?,   // time of first CREATE entry, null if absent
    val liveText: String,
    val isDeleted: Boolean,
    val entries: List<LogHistoryEntry>  // ascending editedAt order
)

data class CurrentRecord(
    val type: HitchLogRecordType,
    val formattedTime: String,
    val text: String,
    val isDeleted: Boolean
)

data class HistoryData(
    val byTimeGroups: List<DateGroup>,
    val byRecordGroups: List<RecordGroup>
)
