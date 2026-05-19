package org.gmautostop.hitchlogmp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.domain.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response

class LogHistoryViewModel(
    private val repository: Repository,
    private val logId: String
) : ViewModel() {

    private val _state = MutableStateFlow<ViewState<List<Pair<String, HitchLogRecordHistoryEntry>>>>(ViewState.Loading)
    val state: StateFlow<ViewState<List<Pair<String, HitchLogRecordHistoryEntry>>>> = _state

    init {
        viewModelScope.launch {
            _state.value = ViewState.Loading

            repository.getLogHistory(logId)
                .distinctUntilChanged()
                .collect { response ->
                    _state.value = when (response) {
                        is Response.Loading -> ViewState.Loading
                        is Response.Failure -> ViewState.Error(response.error)
                        is Response.Success -> ViewState.Show(response.data)
                    }
                }
        }
    }
}
