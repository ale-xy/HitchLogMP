package org.gmautostop.hitchlogmp.ui.history

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.domain.ChangeType
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType

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

data class RecordVersionUi(
    val historyId: String,
    val editedAt: Instant,          // kept for sorting
    val formattedEditedAt: String,
    val changeType: ChangeType,
    val before: RecordFields?,      // null for CREATE (no prior state)
    val after: RecordFields?        // null for DELETE (record no longer exists)
)

data class LogHistoryEntryUi(
    val historyId: String,
    val editedAt: Instant,          // kept for sorting
    val formattedEditedAt: String,
    val recordId: String,
    val changeType: ChangeType,
    val recordType: HitchLogRecordType,   // live (latest) type for icon chip
    val before: RecordFields?,
    val after: RecordFields?
)

data class DateGroupUi(
    val dateLabel: String,
    val date: LocalDate,
    val entries: List<LogHistoryEntryUi>
)

data class RecordGroupUi(
    val recordId: String,
    val liveType: HitchLogRecordType,
    val formattedOriginalTime: String?,   // time of first CREATE entry, null if absent
    val liveText: String,
    val isDeleted: Boolean,
    val entries: List<LogHistoryEntryUi>  // ascending editedAt order
)

data class CurrentRecordUi(
    val type: HitchLogRecordType,
    val formattedTime: String,
    val text: String,
    val isDeleted: Boolean
)

data class LogHistoryData(
    val byTimeGroups: List<DateGroupUi>,
    val byRecordGroups: List<RecordGroupUi>
)
