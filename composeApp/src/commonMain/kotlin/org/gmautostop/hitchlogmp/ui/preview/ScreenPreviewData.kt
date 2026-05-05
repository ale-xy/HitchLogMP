package org.gmautostop.hitchlogmp.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleFinishedRecords
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleHitchLog
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleHitchLogRecords
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleHitchLogState
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleHitchLogs
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleInCarRecords
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleMinimalRecords
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleOffsideRecords
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleRecord
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleRestRecords
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleRetiredRecords
import org.gmautostop.hitchlogmp.ui.hitchlog.HitchLogState
import org.gmautostop.hitchlogmp.ui.recordedit.EditRecordUiState
import org.gmautostop.hitchlogmp.ui.ViewState

// LogList states
class LogListStateProvider : PreviewParameterProvider<ViewState<List<HitchLog>>> {
    override val values = sequenceOf(
        ViewState.Loading,
        ViewState.Show(emptyList()),
        ViewState.Show(sampleHitchLogs()),
        ViewState.Error(AppError.NetworkError("Не удалось загрузить логи"))
    )
}

// HitchLog screen states
class HitchLogStateProvider : PreviewParameterProvider<ViewState<HitchLogState>> {
    override val values = sequenceOf(
        ViewState.Loading,
        ViewState.Show(sampleHitchLogState(records = emptyList())),
        ViewState.Show(sampleHitchLogState(records = sampleMinimalRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleHitchLogRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleInCarRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleRestRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleOffsideRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleFinishedRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleRetiredRecords())),
        ViewState.Error(AppError.NotFound)
    )
}

// EditLog states
class EditLogStateProvider : PreviewParameterProvider<ViewState<HitchLog>> {
    override val values = sequenceOf(
        ViewState.Loading,
        ViewState.Show(sampleHitchLog(id = "", name = "")), // New log
        ViewState.Show(sampleHitchLog(name = "Москва → Санкт-Петербург")), // Existing log
        ViewState.Error(AppError.NetworkError("Ошибка сохранения"))
    )
}

// EditRecord states
class EditRecordStateProvider : PreviewParameterProvider<EditRecordUiState> {
    override val values = sequenceOf(
        // New LIFT record
        EditRecordUiState(
            record = sampleRecord(id = "", type = HitchLogRecordType.LIFT),
            dateText = "29.04.2026",
            timeText = "14:30",
            validationError = null,
            isLoading = false,
            error = null,
            restOnTime = null,
            restElapsedMinutes = null,
            canSave = true
        ),
        // Existing CHECKPOINT record with delete button
        EditRecordUiState(
            record = sampleRecord(
                id = "existing-id",
                type = HitchLogRecordType.CHECKPOINT,
                text = "КП-1 Сестрорецк"
            ),
            dateText = "29.04.2026",
            timeText = "14:30",
            validationError = null,
            isLoading = false,
            error = null,
            restOnTime = null,
            restElapsedMinutes = null,
            canSave = true
        ),
        // REST_OFF with banner
        EditRecordUiState(
            record = sampleRecord(id = "", type = HitchLogRecordType.REST_OFF),
            dateText = "29.04.2026",
            timeText = "14:30",
            validationError = null,
            isLoading = false,
            error = null,
            restOnTime = LocalDateTime(2026, 4, 29, 13, 45),
            restElapsedMinutes = 45,
            canSave = true
        ),
        // With validation errors
        EditRecordUiState(
            record = sampleRecord(id = "", type = HitchLogRecordType.LIFT),
            dateText = "32.13.2026",
            timeText = "25:99",
            validationError = "Неверный формат даты и времени",
            isLoading = false,
            error = null,
            restOnTime = null,
            restElapsedMinutes = null,
            canSave = false
        ),
        // Loading state
        EditRecordUiState(
            record = sampleRecord(),
            dateText = "29.04.2026",
            timeText = "14:30",
            validationError = null,
            isLoading = true,
            error = null,
            restOnTime = null,
            restElapsedMinutes = null,
            canSave = false
        )
    )
}
