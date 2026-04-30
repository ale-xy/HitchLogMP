package org.gmautostop.hitchlogmp.ui.hitchlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.MimeTypes
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.gmautostop.hitchlogmp.domain.computeLiveState
import org.gmautostop.hitchlogmp.domain.computeRestMinutes
import org.gmautostop.hitchlogmp.domain.formatAsCsv
import org.gmautostop.hitchlogmp.domain.formatAsHtml
import org.gmautostop.hitchlogmp.domain.formatAsTxt
import org.gmautostop.hitchlogmp.domain.nextActionLadder
import org.gmautostop.hitchlogmp.shareFile
import org.gmautostop.hitchlogmp.ui.viewmodel.ViewState
import org.lighthousegames.logging.logging

@OptIn(ExperimentalCoroutinesApi::class)
class HitchLogViewModel(
    private val logId: String,
    private val repository: Repository
) : ViewModel() {
    
    private val _state = MutableStateFlow<ViewState<HitchLogState>>(ViewState.Loading)
    val state: StateFlow<ViewState<HitchLogState>> = _state

    sealed interface ExportEvent {
        data object Preparing : ExportEvent
        data class Error(val message: String) : ExportEvent
    }

    private val _exportEvents = MutableSharedFlow<ExportEvent>()
    val exportEvents: SharedFlow<ExportEvent> = _exportEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getLog(logId)
                .distinctUntilChanged()
                .flatMapLatest { logResponse ->
                    when (logResponse) {
                        is Response.Loading -> flowOf(ViewState.Loading)
                        is Response.Failure -> flowOf(ViewState.Error(logResponse.error))
                        is Response.Success -> repository.getLogRecords(logId).map { recordResponse ->
                            when (recordResponse) {
                                is Response.Loading -> ViewState.Loading
                                is Response.Failure -> ViewState.Error(recordResponse.error)
                                is Response.Success -> {
                                    val records = recordResponse.data
                                    ViewState.Show(
                                        HitchLogState(
                                            logName = logResponse.data.name,
                                            teamId = logResponse.data.teamId,
                                            records = records,
                                            summary = SummaryCardState(
                                                lifts = records.count { it.type == HitchLogRecordType.LIFT },
                                                checkpoints = records.count { it.type == HitchLogRecordType.CHECKPOINT },
                                                restMin = computeRestMinutes(records),
                                                liveState = computeLiveState(records)
                                            ),
                                            ladder = nextActionLadder(records)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                .collect { _state.value = it }
        }
    }

    fun exportAsTxt() = exportAs(MimeTypes.TEXT_PLAIN, "txt", ::formatAsTxt)
    
    fun exportAsCsv() = exportAs(MimeTypes.TEXT_CSV, "csv", ::formatAsCsv)
    
    fun exportAsHtml() = exportAs(MimeTypes.TEXT_HTML, "html", ::formatAsHtml)

    private fun exportAs(
        mimeType: String,
        extension: String,
        formatter: (HitchLog, List<HitchLogRecord>) -> String
    ) {
        viewModelScope.launch {
            try {
                _exportEvents.emit(ExportEvent.Preparing)
                val currentState = state.value
                if (currentState is ViewState.Show) {
                    val hitchLogState = currentState.value
                    val log = HitchLog(
                        id = logId,
                        name = hitchLogState.logName,
                        teamId = hitchLogState.teamId
                    )
                    val content = withContext(Dispatchers.Default) {
                        formatter(log, hitchLogState.records)
                    }
                    shareFile(content, mimeType, "${log.name}.$extension")
                }
            } catch (e: Exception) {
                logger.e(e) { "Export $extension failed" }
                _exportEvents.emit(ExportEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }

    companion object {
        val logger = logging()
    }
}
