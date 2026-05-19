package org.gmautostop.hitchlogmp.ui.history

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.domain.ChangeType
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType

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
 * Preview parameter provider for LogHistoryEntryUi with different change types.
 */
class LogHistoryEntryUiProvider : PreviewParameterProvider<LogHistoryEntryUi> {
    override val values: Sequence<LogHistoryEntryUi> = sequenceOf(
        // CREATE
        LogHistoryEntryUi(
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
        LogHistoryEntryUi(
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
 * Preview parameter provider for RecordGroupUi.
 */
class RecordGroupUiProvider : PreviewParameterProvider<RecordGroupUi> {
    override val values: Sequence<RecordGroupUi> = sequenceOf(
        // Active record
        RecordGroupUi(
            recordId = "r1",
            liveType = HitchLogRecordType.LIFT,
            formattedOriginalTime = "14:30",
            liveText = "Попутчик из Москвы",
            isDeleted = false,
            entries = listOf(
                LogHistoryEntryUi(
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
                LogHistoryEntryUi(
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
        RecordGroupUi(
            recordId = "r2",
            liveType = HitchLogRecordType.CHECKPOINT,
            formattedOriginalTime = "12:00",
            liveText = "",
            isDeleted = true,
            entries = listOf(
                LogHistoryEntryUi(
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
                LogHistoryEntryUi(
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
 * Preview parameter provider for CurrentRecordUi.
 */
class CurrentRecordUiProvider : PreviewParameterProvider<CurrentRecordUi> {
    override val values: Sequence<CurrentRecordUi> = sequenceOf(
        // Active record
        CurrentRecordUi(
            type = HitchLogRecordType.LIFT,
            formattedTime = "14:30",
            text = "Попутчик из Москвы",
            isDeleted = false
        ),
        // Deleted record
        CurrentRecordUi(
            type = HitchLogRecordType.MEET,
            formattedTime = "16:00",
            text = "",
            isDeleted = true
        )
    )
}

/**
 * Preview parameter provider for RecordVersionUi with different change types.
 */
class RecordVersionUiProvider : PreviewParameterProvider<RecordVersionUi> {
    override val values: Sequence<RecordVersionUi> = sequenceOf(
        // CREATE
        RecordVersionUi(
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
        RecordVersionUi(
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
        RecordVersionUi(
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
 * Preview parameter provider for LogHistoryData with different states.
 */
class LogHistoryDataProvider : PreviewParameterProvider<LogHistoryData> {
    override val values: Sequence<LogHistoryData> = sequenceOf(
        // With data
        LogHistoryData(
            byTimeGroups = listOf(
                DateGroupUi(
                    date = kotlinx.datetime.LocalDate(2025, 5, 19),
                    dateLabel = "19 мая 2025",
                    entries = listOf(
                        LogHistoryEntryUi(
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
                        LogHistoryEntryUi(
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
                RecordGroupUi(
                    recordId = "r1",
                    liveType = HitchLogRecordType.LIFT,
                    formattedOriginalTime = "14:30",
                    liveText = "Попутчик из Москвы",
                    isDeleted = false,
                    entries = listOf(
                        LogHistoryEntryUi(
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
                        LogHistoryEntryUi(
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
        LogHistoryData(
            byTimeGroups = emptyList(),
            byRecordGroups = emptyList()
        )
    )
}
