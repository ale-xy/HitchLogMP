package org.gmautostop.hitchlogmp.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
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
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogState
import org.gmautostop.hitchlogmp.ui.viewmodel.ViewState

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
data class RecordEditPreviewState(
    val record: HitchLogRecord,
    val dateText: String,
    val timeText: String,
    val error: AppError?,
    val isLoading: Boolean
)

class RecordEditStateProvider : PreviewParameterProvider<RecordEditPreviewState> {
    override val values = sequenceOf(
        RecordEditPreviewState(
            record = sampleRecord(id = "", type = org.gmautostop.hitchlogmp.domain.HitchLogRecordType.LIFT),
            dateText = "28.04.2026",
            timeText = "14:30",
            error = null,
            isLoading = false
        ),
        RecordEditPreviewState(
            record = sampleRecord(type = org.gmautostop.hitchlogmp.domain.HitchLogRecordType.CHECKPOINT, text = "КП1 - Владимир"),
            dateText = "28.04.2026",
            timeText = "16:45",
            error = null,
            isLoading = false
        ),
        RecordEditPreviewState(
            record = sampleRecord(type = org.gmautostop.hitchlogmp.domain.HitchLogRecordType.FREE_TEXT),
            dateText = "28.04.2026",
            timeText = "18:00",
            error = AppError.ParseError("дата"),
            isLoading = false
        ),
        RecordEditPreviewState(
            record = sampleRecord(),
            dateText = "28.04.2026",
            timeText = "14:30",
            error = null,
            isLoading = true
        )
    )
}
