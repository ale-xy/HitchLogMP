package org.gmautostop.hitchlogmp.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response

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

    private val _state = MutableStateFlow<ViewState<HitchLogRecord>>(ViewState.Loading)
    val state = _state.asStateFlow()

    val record = mutableStateOf(HitchLogRecord())

    val date = mutableStateOf<String>("")
    val time = mutableStateOf<String>("")

    init {
        if (recordId.isNullOrEmpty()) {
            record.value = HitchLogRecord(type = itemType).also {
                date.value = dateFormat.format(it.time)
                time.value = timeFormat.format(it.time)
            }
            _state.value = ViewState.Show(record.value)
        } else {
            viewModelScope.launch {
                repository.getRecord(logId, recordId).distinctUntilChanged().collect { response ->
                    when (response) {
                        is Response.Loading -> _state.value = ViewState.Loading
                        is Response.Success<HitchLogRecord> -> {
                            response.data.let {
                                _state.value = ViewState.Show(it)
                                record.value = it
                                date.value = dateFormat.format(it.time)
                                time.value = timeFormat.format(it.time)
                            }
                        }
                        is Response.Failure -> _state.value =
                            ViewState.Error(response.errorMessage)
                    }
                }
            }
        }
    }

    fun updateDate(date: String) {
        this.date.value = date
    }

    fun updateTime(time: String) {
        this.time.value = time
    }

    private fun saveDate() {
        try {
            dateTimeFormat.parse("${date.value} ${time.value}").let {
                record.value = record.value.copy(time = it)
            }
        } catch (e: IllegalArgumentException) {}
    }

    fun updateText(text:String) {
        record.value = record.value.copy(text = text)
    }

    fun save() {
        saveDate()
        _state.value = ViewState.Loading
        repository.saveRecord(logId, record.value).launchIn(viewModelScope)
        //todo error
    }

    fun delete() = repository.deleteRecord(logId, record.value).launchIn(viewModelScope)
}