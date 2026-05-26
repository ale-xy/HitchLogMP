package org.gmautostop.hitchlogmp.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.dateTimeFormat
import org.gmautostop.hitchlogmp.domain.history.buildLogHistoryData
import org.gmautostop.hitchlogmp.domain.model.HistoryData
import org.gmautostop.hitchlogmp.domain.model.SortMode
import org.gmautostop.hitchlogmp.domain.repository.Repository
import org.gmautostop.hitchlogmp.domain.repository.Response
import org.gmautostop.hitchlogmp.formatDateLocale
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.ViewState

class LogHistoryViewModel(
    private val repository: Repository,
    private val logId: String
) : ViewModel() {

    val state: StateFlow<ViewState<HistoryData>>
        field = MutableStateFlow<ViewState<HistoryData>>(ViewState.Loading)

    val sortMode: StateFlow<SortMode>
        field = MutableStateFlow(SortMode.BY_RECORD)

    fun setSortMode(mode: SortMode) {
        sortMode.value = mode
    }

    init {
        viewModelScope.launch {
            state.value = ViewState.Loading

            repository.getLogHistory(logId)
                .distinctUntilChanged()
                .collect { response ->
                    state.value = when (response) {
                        is Response.Loading -> ViewState.Loading
                        is Response.Failure -> ViewState.Error(response.error)
                        is Response.Success -> ViewState.Show(
                            buildLogHistoryData(
                                rawData = response.data,
                                formatDate = { formatDateLocale(it) },
                                formatTime = { timeFormatForDisplay.format(it) },
                                formatEditedAt = { dateTimeFormat.format(it) },
                                useNumericDateFormat = false
                            )
                        )
                    }
                }
        }
    }

}
