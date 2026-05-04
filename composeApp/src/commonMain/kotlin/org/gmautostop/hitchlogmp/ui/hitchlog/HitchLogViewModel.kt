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
import org.gmautostop.hitchlogmp.domain.formatAsXlsxRows
import org.gmautostop.hitchlogmp.domain.generateXlsxBytes
import org.gmautostop.hitchlogmp.domain.nextActionLadder
import org.gmautostop.hitchlogmp.shareFile
import org.gmautostop.hitchlogmp.shareFileBytes
import org.gmautostop.hitchlogmp.ui.viewmodel.ViewState
import org.lighthousegames.logging.logging

@OptIn(ExperimentalCoroutinesApi::class)
class HitchLogViewModel(
    private val logId: String,
    private val repository: Repository
) : ViewModel() {
    
    val state: StateFlow<ViewState<HitchLogState>>
        field = MutableStateFlow<ViewState<HitchLogState>>(ViewState.Loading)

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
                .collect { state.value = it }
        }
    }

    fun exportAsTxt() = exportAs(MimeTypes.TEXT_PLAIN, "txt") { log, records ->
        formatAsTxt(log, records)
    }
    
    fun exportAsCsv() = exportAs(MimeTypes.TEXT_CSV, "csv") { log, records ->
        formatAsCsv(log, records)
    }
    
    fun exportAsHtml() = exportAs(MimeTypes.TEXT_HTML, "html") { log, records ->
        formatAsHtml(log, records)
    }

    fun exportAsXlsx() = exportAs(MimeTypes.APPLICATION_XLSX, "xlsx") { log, records ->
        val rows = formatAsXlsxRows(log, records)
        generateXlsxBytes(rows)
    }

    private fun exportAs(
        mimeType: String,
        extension: String,
        formatter: suspend (HitchLog, List<HitchLogRecord>) -> Any
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
                    val fileName = sanitizeFileName(log.name)
                    when (content) {
                        is String -> shareFile(content, mimeType, "$fileName.$extension")
                        is ByteArray -> shareFileBytes(content, mimeType, "$fileName.$extension")
                        else -> throw IllegalStateException("Unsupported export format: ${content::class}")
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Export $extension failed" }
                _exportEvents.emit(ExportEvent.Error(e.message ?: "Unknown error"))
            }
        }
    }

    /**
     * Sanitizes a file name by trimming whitespace and replacing invalid characters.
     */
    private fun sanitizeFileName(name: String): String {
        return name.trim()
            .replace(Regex("[/\\\\:*?\"<>|]"), "_")
            .ifEmpty { "export" }
    }

    companion object {
        val logger = logging()
    }
}
