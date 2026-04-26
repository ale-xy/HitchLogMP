package org.gmautostop.hitchlogmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response

data class EditRecordUiState(
    val record: HitchLogRecord = HitchLogRecord(),
    val dateText: String = "",
    val timeText: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

@OptIn(FormatStringsInDatetimeFormats::class)
class RecordViewModel(
    private val repository: Repository,
    private val logId: String,
    private val recordId: String? = null,
    private val itemType: HitchLogRecordType = HitchLogRecordType.FREE_TEXT
): ViewModel() {
    private val dateFormat = LocalDateTime.Format { byUnicodePattern("dd.MM.yyyy") }
    private val timeFormat = LocalDateTime.Format { byUnicodePattern("HH:mm") }
    private val dateTimeFormat = LocalDateTime.Format { byUnicodePattern("dd.MM.yyyy HH:mm") }

    val uiState: StateFlow<EditRecordUiState>
        field = MutableStateFlow(EditRecordUiState())

    private val _navigationEvent = Channel<Unit>(Channel.CONFLATED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        if (recordId.isNullOrEmpty()) {
            val newRecord = HitchLogRecord(type = itemType)
            uiState.value = EditRecordUiState(
                record = newRecord,
                dateText = dateFormat.format(newRecord.time),
                timeText = timeFormat.format(newRecord.time),
                isLoading = false
            )
        } else {
            viewModelScope.launch {
                repository.getRecord(logId, recordId).distinctUntilChanged().collect { response ->
                    uiState.update { current ->
                        when (response) {
                            is Response.Loading -> current.copy(isLoading = true, error = null)
                            is Response.Success -> EditRecordUiState(
                                record = response.data,
                                dateText = dateFormat.format(response.data.time),
                                timeText = timeFormat.format(response.data.time),
                                isLoading = false
                            )
                            is Response.Failure -> current.copy(isLoading = false, error = response.errorMessage)
                        }
                    }
                }
            }
        }
    }

    fun updateDate(date: String) {
        uiState.update { it.copy(dateText = date) }
    }

    fun updateTime(time: String) {
        uiState.update { it.copy(timeText = time) }
    }

    fun updateText(text: String) {
        uiState.update { it.copy(record = it.record.copy(text = text)) }
    }

    fun save() {
        val current = uiState.value
        val recordToSave = try {
            current.record.copy(time = dateTimeFormat.parse("${current.dateText} ${current.timeText}"))
        } catch (e: IllegalArgumentException) {
            current.record
        }
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            repository.saveRecord(logId, recordToSave).collect { response ->
                when (response) {
                    is Response.Loading -> Unit
                    is Response.Success -> _navigationEvent.send(Unit)
                    is Response.Failure -> uiState.update { it.copy(isLoading = false, error = response.errorMessage) }
                }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            uiState.update { it.copy(isLoading = true, error = null) }
            repository.deleteRecord(logId, uiState.value.record).collect { response ->
                when (response) {
                    is Response.Loading -> Unit
                    is Response.Success -> _navigationEvent.send(Unit)
                    is Response.Failure -> uiState.update { it.copy(isLoading = false, error = response.errorMessage) }
                }
            }
        }
    }
}
