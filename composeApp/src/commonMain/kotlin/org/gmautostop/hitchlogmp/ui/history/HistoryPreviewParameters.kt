package org.gmautostop.hitchlogmp.ui.history

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.domain.model.ChangeType
import org.gmautostop.hitchlogmp.domain.model.CurrentRecord
import org.gmautostop.hitchlogmp.domain.model.DateGroup
import org.gmautostop.hitchlogmp.domain.model.HistoryData
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.model.LogHistoryEntry
import org.gmautostop.hitchlogmp.domain.model.RecordFields
import org.gmautostop.hitchlogmp.domain.model.RecordGroup
import org.gmautostop.hitchlogmp.domain.model.RecordVersion

/**
 * Preview parameter provider for RecordFields with text.
 */
class RecordFieldsWithTextProvider : PreviewParameterProvider<RecordFields> {
    override val values: Sequence<RecordFields> = sequenceOf(
        RecordFields(
            time = LocalDateTime(2025, 5, 19, 14, 30, 0),
            formattedTime = "14:30",
            type = HitchLogRecordType.LIFT,
            text = "Попутчик из Москвы"
        )
    )
}

/**
 * Preview parameter provider for RecordFields without text.
 */
class RecordFieldsNoTextProvider : PreviewParameterProvider<RecordFields> {
    override val values: Sequence<RecordFields> = sequenceOf(
        RecordFields(
            time = LocalDateTime(2025, 5, 19, 10, 0, 0),
            formattedTime = "10:00",
            type = HitchLogRecordType.WALK,
            text = null
        )
    )
}

/**
 * Preview parameter provider for LogHistoryEntry with different change types.
 */
class LogHistoryEntryProvider : PreviewParameterProvider<LogHistoryEntry> {
    override val values: Sequence<LogHistoryEntry> = sequenceOf(
        // CREATE
        LogHistoryEntry(
            historyId = "h1",
            editedAt = Instant.fromEpochMilliseconds(0),
            formattedEditedAt = "14:30",
            recordId = "r1",
            changeType = ChangeType.CREATE,
            recordType = HitchLogRecordType.LIFT,
            before = null,
            after = RecordFields(
                time = LocalDateTime(2025, 5, 19, 14, 30, 0),
                formattedTime = "14:30",
                type = HitchLogRecordType.LIFT,
                text = "Попутчик из Москвы"
            )
        ),
        // UPDATE
        LogHistoryEntry(
            historyId = "h2",
            editedAt = Instant.fromEpochMilliseconds(0),
            formattedEditedAt = "15:00",
            recordId = "r1",
            changeType = ChangeType.UPDATE,
            recordType = HitchLogRecordType.LIFT,
            before = RecordFields(
                time = LocalDateTime(2025, 5, 19, 14, 0, 0),
                formattedTime = "14:00",
                type = HitchLogRecordType.WALK,
                text = null
            ),
            after = RecordFields(
                time = LocalDateTime(2025, 5, 19, 14, 30, 0),
                formattedTime = "14:30",
                type = HitchLogRecordType.LIFT,
                text = "Попутчик из Москвы"
            )
        )
    )
}

/**
 * Preview parameter provider for RecordGroup.
 */
class RecordGroupProvider : PreviewParameterProvider<RecordGroup> {
    override val values: Sequence<RecordGroup> = sequenceOf(
        // Active record
        RecordGroup(
            recordId = "r1",
            liveType = HitchLogRecordType.LIFT,
            formattedOriginalTime = "14:30",
            liveText = "Попутчик из Москвы",
            isDeleted = false,
            entries = listOf(
                LogHistoryEntry(
                    "h1",
                    Instant.fromEpochMilliseconds(0),
                    "19.05.2025 14:00",
                    "r1",
                    ChangeType.CREATE,
                    HitchLogRecordType.LIFT,
                    null,
                    RecordFields(
                        LocalDateTime(2025, 5, 19, 14, 0, 0),
                        "14:00",
                        HitchLogRecordType.WALK,
                        null
                    )
                ),
                LogHistoryEntry(
                    "h2",
                    Instant.fromEpochMilliseconds(1000),
                    "19.05.2025 15:00",
                    "r1",
                    ChangeType.UPDATE,
                    HitchLogRecordType.LIFT,
                    RecordFields(
                        LocalDateTime(2025, 5, 19, 14, 0, 0),
                        "14:00",
                        HitchLogRecordType.WALK,
                        null
                    ),
                    RecordFields(
                        LocalDateTime(2025, 5, 19, 14, 30, 0),
                        "14:30",
                        HitchLogRecordType.LIFT,
                        "Попутчик из Москвы"
                    )
                )
            )
        ),
        // Deleted record
        RecordGroup(
            recordId = "r2",
            liveType = HitchLogRecordType.CHECKPOINT,
            formattedOriginalTime = "12:00",
            liveText = "",
            isDeleted = true,
            entries = listOf(
                LogHistoryEntry(
                    "h3",
                    Instant.fromEpochMilliseconds(0),
                    "19.05.2025 12:00",
                    "r2",
                    ChangeType.CREATE,
                    HitchLogRecordType.CHECKPOINT,
                    null,
                    RecordFields(
                        LocalDateTime(2025, 5, 19, 12, 0, 0),
                        "12:00",
                        HitchLogRecordType.CHECKPOINT,
                        null
                    )
                ),
                LogHistoryEntry(
                    "h4",
                    Instant.fromEpochMilliseconds(2000),
                    "19.05.2025 13:00",
                    "r2",
                    ChangeType.DELETE,
                    HitchLogRecordType.CHECKPOINT,
                    RecordFields(
                        LocalDateTime(2025, 5, 19, 12, 0, 0),
                        "12:00",
                        HitchLogRecordType.CHECKPOINT,
                        null
                    ),
                    null
                )
            )
        )
    )
}

/**
 * Preview parameter provider for CurrentRecord.
 */
class CurrentRecordProvider : PreviewParameterProvider<CurrentRecord> {
    override val values: Sequence<CurrentRecord> = sequenceOf(
        // Active record
        CurrentRecord(
            type = HitchLogRecordType.LIFT,
            formattedTime = "14:30",
            text = "Попутчик из Москвы",
            isDeleted = false
        ),
        // Deleted record
        CurrentRecord(
            type = HitchLogRecordType.MEET,
            formattedTime = "16:00",
            text = "",
            isDeleted = true
        )
    )
}

/**
 * Preview parameter provider for RecordVersion with different change types.
 */
class RecordVersionProvider : PreviewParameterProvider<RecordVersion> {
    override val values: Sequence<RecordVersion> = sequenceOf(
        // CREATE
        RecordVersion(
            historyId = "h1",
            editedAt = Instant.fromEpochMilliseconds(0),
            formattedEditedAt = "19.05.2025 14:30",
            changeType = ChangeType.CREATE,
            before = null,
            after = RecordFields(
                time = LocalDateTime(2025, 5, 19, 14, 30, 0),
                formattedTime = "14:30",
                type = HitchLogRecordType.LIFT,
                text = "Попутчик из Москвы"
            )
        ),
        // UPDATE
        RecordVersion(
            historyId = "h2",
            editedAt = Instant.fromEpochMilliseconds(0),
            formattedEditedAt = "19.05.2025 15:00",
            changeType = ChangeType.UPDATE,
            before = RecordFields(
                time = LocalDateTime(2025, 5, 19, 14, 0, 0),
                formattedTime = "14:00",
                type = HitchLogRecordType.WALK,
                text = null
            ),
            after = RecordFields(
                time = LocalDateTime(2025, 5, 19, 14, 30, 0),
                formattedTime = "14:30",
                type = HitchLogRecordType.LIFT,
                text = "Попутчик из Москвы"
            )
        ),
        // DELETE
        RecordVersion(
            historyId = "h3",
            editedAt = Instant.fromEpochMilliseconds(0),
            formattedEditedAt = "19.05.2025 16:00",
            changeType = ChangeType.DELETE,
            before = RecordFields(
                time = LocalDateTime(2025, 5, 19, 14, 30, 0),
                formattedTime = "14:30",
                type = HitchLogRecordType.LIFT,
                text = "Попутчик из Москвы"
            ),
            after = null
        )
    )
}

/**
 * Preview parameter provider for HistoryData with different states.
 */
class HistoryDataProvider : PreviewParameterProvider<HistoryData> {
    override val values: Sequence<HistoryData> = sequenceOf(
        // With data
        HistoryData(
            byTimeGroups = listOf(
                DateGroup(
                    date = kotlinx.datetime.LocalDate(2025, 5, 19),
                    dateLabel = "19 мая 2025",
                    entries = listOf(
                        LogHistoryEntry(
                            historyId = "h1",
                            editedAt = Instant.fromEpochMilliseconds(0),
                            formattedEditedAt = "14:30",
                            recordId = "r1",
                            changeType = ChangeType.CREATE,
                            recordType = HitchLogRecordType.LIFT,
                            before = null,
                            after = RecordFields(
                                time = LocalDateTime(2025, 5, 19, 14, 30, 0),
                                formattedTime = "14:30",
                                type = HitchLogRecordType.LIFT,
                                text = "Попутчик из Москвы"
                            )
                        ),
                        LogHistoryEntry(
                            historyId = "h2",
                            editedAt = Instant.fromEpochMilliseconds(1000),
                            formattedEditedAt = "15:00",
                            recordId = "r1",
                            changeType = ChangeType.UPDATE,
                            recordType = HitchLogRecordType.LIFT,
                            before = RecordFields(
                                time = LocalDateTime(2025, 5, 19, 14, 0, 0),
                                formattedTime = "14:00",
                                type = HitchLogRecordType.WALK,
                                text = null
                            ),
                            after = RecordFields(
                                time = LocalDateTime(2025, 5, 19, 14, 30, 0),
                                formattedTime = "14:30",
                                type = HitchLogRecordType.LIFT,
                                text = "Попутчик из Москвы"
                            )
                        )
                    )
                )
            ),
            byRecordGroups = listOf(
                RecordGroup(
                    recordId = "r1",
                    liveType = HitchLogRecordType.LIFT,
                    formattedOriginalTime = "14:30",
                    liveText = "Попутчик из Москвы",
                    isDeleted = false,
                    entries = listOf(
                        LogHistoryEntry(
                            "h1",
                            Instant.fromEpochMilliseconds(0),
                            "19.05.2025 14:00",
                            "r1",
                            ChangeType.CREATE,
                            HitchLogRecordType.LIFT,
                            null,
                            RecordFields(
                                LocalDateTime(2025, 5, 19, 14, 0, 0),
                                "14:00",
                                HitchLogRecordType.WALK,
                                null
                            )
                        ),
                        LogHistoryEntry(
                            "h2",
                            Instant.fromEpochMilliseconds(1000),
                            "19.05.2025 15:00",
                            "r1",
                            ChangeType.UPDATE,
                            HitchLogRecordType.LIFT,
                            RecordFields(
                                LocalDateTime(2025, 5, 19, 14, 0, 0),
                                "14:00",
                                HitchLogRecordType.WALK,
                                null
                            ),
                            RecordFields(
                                LocalDateTime(2025, 5, 19, 14, 30, 0),
                                "14:30",
                                HitchLogRecordType.LIFT,
                                "Попутчик из Москвы"
                            )
                        )
                    )
                )
            )
        ),
        // Empty
        HistoryData(
            byTimeGroups = emptyList(),
            byRecordGroups = emptyList()
        )
    )
}
