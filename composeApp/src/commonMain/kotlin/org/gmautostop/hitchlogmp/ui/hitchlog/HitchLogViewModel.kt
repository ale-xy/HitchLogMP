package org.gmautostop.hitchlogmp.ui.hitchlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.gmautostop.hitchlogmp.domain.logic.computeLiveRestMinutes
import org.gmautostop.hitchlogmp.domain.logic.computeLiveState
import org.gmautostop.hitchlogmp.domain.logic.computeRestDivisions
import org.gmautostop.hitchlogmp.domain.logic.computeRestDivisionsLeft
import org.gmautostop.hitchlogmp.domain.logic.computeRestLeft
import org.gmautostop.hitchlogmp.domain.logic.formatMinutes
import org.gmautostop.hitchlogmp.domain.logic.nextActionLadder
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.repository.Repository
import org.gmautostop.hitchlogmp.domain.repository.Response
import org.gmautostop.hitchlogmp.export.ChronicleFormatter
import org.gmautostop.hitchlogmp.export.ExportFormat
import org.gmautostop.hitchlogmp.export.FileSaver
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.ui.ViewState
import org.gmautostop.hitchlogmp.ui.export.ExportStrings
import org.gmautostop.hitchlogmp.ui.export.buildExportStrings
import org.lighthousegames.logging.logging
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.copy
import kotlin.collections.count
import kotlin.collections.isNotEmpty
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class HitchLogViewModel(
    private val logId: String,
    private val repository: Repository,
    private val formatters: Map<ExportFormat, ChronicleFormatter>,
    private val fileSaver: FileSaver
) : ViewModel() {
    
    val state: StateFlow<ViewState<HitchLogState>>
        field = MutableStateFlow<ViewState<HitchLogState>>(ViewState.Loading)

    private val _currentTime = MutableStateFlow(Clock.System.now())

    private var exportStrings: ExportStrings? = null

    private suspend fun getExportStrings(): ExportStrings {
        return exportStrings ?: run {
            val strings = buildExportStrings()
            exportStrings = strings
            strings
        }
    }

    sealed interface ExportEvent {
        data object Preparing : ExportEvent
        data class Error(val message: String) : ExportEvent
    }

    private val _exportEvents = MutableSharedFlow<ExportEvent>()
    val exportEvents: SharedFlow<ExportEvent> = _exportEvents.asSharedFlow()

    init {
        // Start minute-aligned timer
        viewModelScope.launch {
            val now = Clock.System.now()
            val nowLocal = now.localTZDateTime()
            val secondsIntoMinute = nowLocal.second
            val initialDelay = (60 - secondsIntoMinute) * 1000L
            
            delay(initialDelay)
            _currentTime.value = Clock.System.now()
            
            while (true) {
                delay(60_000)
                _currentTime.value = Clock.System.now()
            }
        }
        
        viewModelScope.launch {
            repository.getLog(logId)
                .distinctUntilChanged()
                .flatMapLatest { logResponse ->
                    when (logResponse) {
                        is Response.Loading -> flowOf(ViewState.Loading)
                        is Response.Failure -> flowOf(ViewState.Error(logResponse.error))
                        is Response.Success -> repository.getLogRecords(logId)
                            .flatMapLatest { recordResponse ->
                                when (recordResponse) {
                                    is Response.Loading -> flowOf(ViewState.Loading)
                                    is Response.Failure -> flowOf(ViewState.Error(recordResponse.error))
                                    is Response.Success -> {
                                        // Combine records with timer for live rest updates
                                        combine(flowOf(recordResponse.data), _currentTime) { records, currentTime ->
                                            val currentTimeLocal = currentTime.localTZDateTime()
                                            val liveState = computeLiveState(records)
                                            
                                            // Calculate rest time (live if on rest, static otherwise)
                                            val restUsedMin = computeLiveRestMinutes(records, currentTimeLocal)
                                            val restUsedDivisions = computeRestDivisions(records)
                                            val restLeftMin = computeRestLeft(records, totalRestMin = null)
                                            val restLeftDivisions = computeRestDivisionsLeft(records, totalRestDivisions = null)
                                            
                                            // Preserve current toggle state across record updates
                                            val currentShowUsed = (state.value as? ViewState.Show<HitchLogState>)
                                                ?.value?.summary?.showUsed ?: true
                                            
                                            ViewState.Show(
                                                HitchLogState(
                                                    logId = logId,
                                                    logName = logResponse.data.name,
                                                    teamId = logResponse.data.teamId,
                                                    records = records,
                                                    summary = SummaryCardState(
                                                        lifts = records.count { it.type == HitchLogRecordType.LIFT },
                                                        checkpoints = records.count { it.type == HitchLogRecordType.CHECKPOINT },
                                                        restUsedDisplay = "${formatMinutes(restUsedMin)}/$restUsedDivisions",
                                                        restLeftDisplay = "${formatMinutes(restLeftMin)}/$restLeftDivisions",
                                                        showUsed = currentShowUsed,
                                                        liveState = liveState
                                                    ),
                                                    ladder = nextActionLadder(records)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                    }
                }
                .collect { state.value = it }
        }
    }

    fun toggleRestDisplay() {
        val currentState = state.value
        if (currentState is ViewState.Show) {
            val currentSummary = currentState.value.summary
            val updatedSummary = currentSummary.copy(
                showUsed = !currentSummary.showUsed
            )
            val updatedHitchLogState = currentState.value.copy(
                summary = updatedSummary
            )
            state.value = ViewState.Show(updatedHitchLogState)
        }
    }

    fun export(format: ExportFormat) {
        viewModelScope.launch {
            try {
                _exportEvents.emit(ExportEvent.Preparing)
                val currentState = state.value
                if (currentState !is ViewState.Show) return@launch
                
                val hitchLogState = currentState.value
                val log = HitchLog(
                    id = logId,
                    name = hitchLogState.logName,
                    teamId = hitchLogState.teamId
                )
                val records = hitchLogState.records
                val history = getHistoryForExport()
                
                val formatter = formatters[format] ?: error("No formatter for $format")
                val strings = getExportStrings()
                val bytes = withContext(Dispatchers.Default) {
                    formatter.format(log, records, history, strings)
                }
                
                val fileName = "${sanitizeFileName(log.name)}.${format.extension}"
                fileSaver.save(fileName, bytes, format)
                
            } catch (e: Exception) {
                logger.e(err = e) { "Export ${format.extension} failed" }
                val errorMsg = e.message?.takeIf { it.isNotBlank() } 
                    ?: e::class.simpleName 
                    ?: "Unknown error"
                _exportEvents.emit(ExportEvent.Error(errorMsg))
            }
        }
    }
    
    private suspend fun getHistoryForExport(): List<Pair<String, HitchLogRecordHistoryEntry>>? {
        return try {
            repository.getLogHistory(logId)
                .first { it is Response.Success || it is Response.Failure }
                .let { response ->
                    when (response) {
                        is Response.Success -> response.data.takeIf { it.isNotEmpty() }
                        else -> null
                    }
                }
        } catch (e: Exception) {
            null  // Non-blocking - export continues without history
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
