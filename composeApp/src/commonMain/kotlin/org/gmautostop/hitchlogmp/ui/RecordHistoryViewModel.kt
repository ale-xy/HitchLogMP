package org.gmautostop.hitchlogmp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.history.CurrentRecordUi
import org.gmautostop.hitchlogmp.ui.history.RecordVersionUi
import org.gmautostop.hitchlogmp.ui.history.computeRecordVersions

class RecordHistoryViewModel(
    private val repository: Repository,
    private val logId: String,
    private val recordId: String
) : ViewModel() {

    val state: StateFlow<ViewState<List<RecordVersionUi>>>
        field = MutableStateFlow<ViewState<List<RecordVersionUi>>>(ViewState.Loading)

    val currentRecordUi: StateFlow<CurrentRecordUi?>
        field = MutableStateFlow<CurrentRecordUi?>(null)

    init {
        viewModelScope.launch {
            state.value = ViewState.Loading
            repository.getRecordHistory(logId, recordId)
                .collect { response ->
                    state.value = when (response) {
                        is Response.Loading -> ViewState.Loading
                        is Response.Failure -> ViewState.Error(response.error)
                        is Response.Success -> ViewState.Show(computeRecordVersions(response.data))
                    }
                }
        }
        viewModelScope.launch {
            repository.getRecord(logId, recordId)
                .collect { response ->
                    if (response is Response.Success) {
                        val data = response.data
                        currentRecordUi.value = CurrentRecordUi(
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
