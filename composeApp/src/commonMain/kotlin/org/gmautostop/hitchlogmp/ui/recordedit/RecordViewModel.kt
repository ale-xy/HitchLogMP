package org.gmautostop.hitchlogmp.ui.recordedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.until
import org.gmautostop.hitchlogmp.dateFormat
import org.gmautostop.hitchlogmp.dateTimeFormat
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.timeFormat
import org.lighthousegames.logging.logging
import kotlin.time.Clock

/**
 * Callback interface for EditRecordScreen interactions.
 * Groups all user actions that modify record state.
 */
interface EditRecordCallbacks {
    fun updateDate(date: String)
    fun updateTime(time: String)
    fun updateText(text: String)
    fun adjustDate(days: Int)
    fun adjustTime(minutes: Int)
    fun setTimeToNow()
    fun save()
    fun delete()
}

data class EditRecordUiState(
    val record: HitchLogRecord = HitchLogRecord(),
    val dateText: String = "",
    val timeText: String = "",
    val validationError: String? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
    
    // REST_OFF banner data (null if not applicable)
    val restOnTime: LocalDateTime? = null,
    val restElapsedMinutes: Int? = null,
    
    // Original time for reset functionality (null for new records)
    val originalTime: LocalDateTime? = null,
    
    // Derived validation state
    val canSave: Boolean = false
)

class EditRecordViewModel(
    private val repository: Repository,
    private val logId: String,
    private val recordId: String? = null,
    private val itemType: HitchLogRecordType = HitchLogRecordType.FREE_TEXT
): ViewModel(), EditRecordCallbacks {

    private val _uiState = MutableStateFlow(EditRecordUiState())
    val uiState: StateFlow<EditRecordUiState> = _uiState

    private val _navigationEvent = Channel<Unit>(Channel.CONFLATED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    // For REST_OFF real-time clock
    private val _currentTime = MutableStateFlow(Clock.System.now())

    init {
        if (recordId.isNullOrEmpty()) {
            val newRecord = HitchLogRecord(type = itemType)
            _uiState.value = EditRecordUiState(
                record = newRecord,
                dateText = dateFormat.format(newRecord.time.date),
                timeText = timeFormat.format(newRecord.time.time),
                isLoading = false,
                originalTime = null  // New record, no original time
            )
            validateAndUpdateState()
        } else {
            viewModelScope.launch {
                repository.getRecord(logId, recordId).distinctUntilChanged().collect { response ->
                    _uiState.update { current ->
                        when (response) {
                            is Response.Loading -> current.copy(isLoading = true, error = null)
                            is Response.Success -> EditRecordUiState(
                                record = response.data,
                                dateText = dateFormat.format(response.data.time.date),
                                timeText = timeFormat.format(response.data.time.time),
                                isLoading = false,
                                originalTime = response.data.time  // Store original time for reset
                            )
                            is Response.Failure -> current.copy(isLoading = false, error = response.error)
                        }
                    }
                    // Validate after each state update
                    if (response !is Response.Loading) {
                        validateAndUpdateState()
                    }
                }
            }
        }

        // REST_OFF banner logic
        if (itemType == HitchLogRecordType.REST_OFF) {
            viewModelScope.launch {
                repository.getLogRecords(logId).collect { response ->
                    when (response) {
                        is Response.Success -> {
                            val restOnRecord = response.data
                                .filter { it.type == HitchLogRecordType.REST_ON }
                                .maxByOrNull { it.time }
                            
                            if (restOnRecord != null) {
                                // Start real-time clock updates
                                viewModelScope.launch {
                                    while (true) {
                                        _currentTime.value = Clock.System.now()
                                        delay(60_000) // Update every minute
                                    }
                                }
                                
                                // Combine restOn + currentTime to compute elapsed
                                combine(_currentTime, _uiState) { now, state ->
                                    val nowLocal = now.localTZDateTime()
                                    val elapsed = restOnRecord.time.toInstant(TimeZone.currentSystemDefault())
                                        .until(nowLocal.toInstant(TimeZone.currentSystemDefault()), DateTimeUnit.MINUTE)
                                    _uiState.update { 
                                        it.copy(
                                            restOnTime = restOnRecord.time,
                                            restElapsedMinutes = elapsed.toInt()
                                        )
                                    }
                                    Unit
                                }.launchIn(viewModelScope)
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }
    }

    private fun validateAndUpdateState() {
        log.d { "validateAndUpdateState() called - dateText=${_uiState.value.dateText}, timeText=${_uiState.value.timeText}, isLoading=${_uiState.value.isLoading}" }
        
        val dateValid = try {
            LocalDate.parse(_uiState.value.dateText, dateFormat)
            log.d { "Date parsed successfully" }
            true
        } catch (e: Exception) {
            log.e(err = e) { "Date parse failed" }
            false
        }
        
        val timeValid = try {
            LocalTime.parse(_uiState.value.timeText, timeFormat)
            log.d { "Time parsed successfully" }
            true
        } catch (e: Exception) {
            log.e(err = e) { "Time parse failed" }
            false
        }
        
        val validationError = when {
            !dateValid && !timeValid -> "Неверный формат даты и времени"
            !dateValid -> "Неверный формат даты"
            !timeValid -> "Неверный формат времени"
            else -> null
        }
        
        val canSave = validationError == null && !_uiState.value.isLoading
        
        log.d { "Validation result: dateValid=$dateValid, timeValid=$timeValid, isLoading=${_uiState.value.isLoading}, canSave=$canSave" }
        
        _uiState.update { 
            it.copy(
                validationError = validationError,
                canSave = canSave
            )
        }
        
        log.d { "State updated: canSave=${_uiState.value.canSave}" }
    }

    override fun updateDate(date: String) {
        _uiState.update { it.copy(dateText = date) }
        validateAndUpdateState()
    }

    override fun updateTime(time: String) {
        _uiState.update { it.copy(timeText = time) }
        validateAndUpdateState()
    }

    override fun updateText(text: String) {
        _uiState.update { it.copy(record = it.record.copy(text = text)) }
    }

    override fun adjustDate(days: Int) {
        val current = try {
            dateTimeFormat.parse("${_uiState.value.dateText} ${_uiState.value.timeText}")
        } catch (e: Exception) {
            Clock.System.now().localTZDateTime()
        }
        
        val adjusted = current.date.plus(days, DateTimeUnit.DAY)
        val newDateTime = LocalDateTime(adjusted, current.time)
        
        _uiState.update { 
            it.copy(dateText = dateFormat.format(newDateTime.date))
        }
        validateAndUpdateState()
    }

    override fun adjustTime(minutes: Int) {
        val current = try {
            dateTimeFormat.parse("${_uiState.value.dateText} ${_uiState.value.timeText}")
        } catch (e: Exception) {
            Clock.System.now().localTZDateTime()
        }
        
        val adjusted = current.toInstant(TimeZone.currentSystemDefault())
            .plus(minutes, DateTimeUnit.MINUTE, TimeZone.currentSystemDefault())
            .localTZDateTime()
        
        _uiState.update { 
            it.copy(
                dateText = dateFormat.format(adjusted.date),
                timeText = timeFormat.format(adjusted.time)
            )
        }
        validateAndUpdateState()
    }

    override fun setTimeToNow() {
        val current = _uiState.value
        val targetTime = if (current.originalTime != null) {
            // Editing existing record - reset to original time
            current.originalTime
        } else {
            // New record - set to current time
            Clock.System.now().localTZDateTime()
        }
        
        _uiState.update { 
            it.copy(
                dateText = dateFormat.format(targetTime.date),
                timeText = timeFormat.format(targetTime.time)
            )
        }
        validateAndUpdateState()
    }

    override fun save() {
        log.d { "save() called" }
        val current = _uiState.value
        log.d { "Current state: canSave=${current.canSave}, dateText=${current.dateText}, timeText=${current.timeText}, recordId=${current.record.id}" }
        
        val recordToSave = try {
            current.record.copy(time = dateTimeFormat.parse("${current.dateText} ${current.timeText}"))
        } catch (e: IllegalArgumentException) {
            log.e(err = e) { "Failed to parse date/time: ${current.dateText} ${current.timeText}" }
            _uiState.update { it.copy(isLoading = false, error = AppError.ParseError("date/time")) }
            return
        }
        
        log.d { "Record to save: $recordToSave" }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            log.d { "Starting saveRecord flow for logId=$logId" }
            repository.saveRecord(logId, recordToSave).collect { response ->
                log.d { "saveRecord response: $response" }
                when (response) {
                    is Response.Loading -> {
                        log.d { "saveRecord Loading" }
                    }
                    is Response.Success -> {
                        log.d { "saveRecord Success - sending navigation event" }
                        _navigationEvent.send(Unit)
                    }
                    is Response.Failure -> {
                        log.e { "saveRecord Failure: ${response.error}" }
                        _uiState.update { it.copy(isLoading = false, error = response.error) }
                    }
                }
            }
        }
    }

    override fun delete() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.deleteRecord(logId, _uiState.value.record).collect { response ->
                when (response) {
                    is Response.Loading -> Unit
                    is Response.Success -> _navigationEvent.send(Unit)
                    is Response.Failure -> _uiState.update { it.copy(isLoading = false, error = response.error) }
                }
            }
        }
    }
    
    companion object {
        private val log = logging()
    }
}
