package org.gmautostop.hitchlogmp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.dateTimeFormat
import org.gmautostop.hitchlogmp.domain.history.computeRecordVersions
import org.gmautostop.hitchlogmp.domain.model.CurrentRecord
import org.gmautostop.hitchlogmp.domain.model.RecordVersion
import org.gmautostop.hitchlogmp.domain.repository.Repository
import org.gmautostop.hitchlogmp.domain.repository.Response
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.ViewState

class RecordHistoryViewModel(
    private val repository: Repository,
    private val logId: String,
    private val recordId: String
) : ViewModel() {

    val state: StateFlow<ViewState<List<RecordVersion>>>
        field = MutableStateFlow<ViewState<List<RecordVersion>>>(ViewState.Loading)

    val currentRecordUi: StateFlow<CurrentRecord?>
        field = MutableStateFlow<CurrentRecord?>(null)

    init {
        viewModelScope.launch {
            state.value = ViewState.Loading
            repository.getRecordHistory(logId, recordId)
                .collect { response ->
                    state.value = when (response) {
                        is Response.Loading -> ViewState.Loading
                        is Response.Failure -> ViewState.Error(response.error)
                        is Response.Success -> ViewState.Show(
                            computeRecordVersions(
                                entries = response.data,
                                formatTime = { timeFormatForDisplay.format(it) },
                                formatEditedAt = { dateTimeFormat.format(it) }
                            ))
                    }
                }
        }
        viewModelScope.launch {
            repository.getRecord(logId, recordId)
                .collect { response ->
                    if (response is Response.Success) {
                        val data = response.data
                        currentRecordUi.value = CurrentRecord(
                            type = data.type,
                            formattedTime = timeFormatForDisplay.format(data.time),
                            text = data.text,
                            isDeleted = data.deleted
                        )
                    }
                }
        }
    }
}
