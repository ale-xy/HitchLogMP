package org.gmautostop.hitchlogmp.ui.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.lighthousegames.logging.logging


data class HitchLogState(
    val log: HitchLog,
    val records: List<HitchLogRecord>
)

class HitchLogViewModel(
//    savedStateHandle: SavedStateHandle,
    private val logId: String,
    repository: Repository
) : ViewModel() {
    val state: StateFlow<ViewState<HitchLogState>>
        field = MutableStateFlow<ViewState<HitchLogState>>(ViewState.Loading)

    init {
        viewModelScope.launch {
            repository.getLog(logId)
                .distinctUntilChanged()
                .onEach { response ->
                    when(response) {
                        is Response.Loading -> state.value = ViewState.Loading
                        is Response.Failure -> state.value = ViewState.Error(response.errorMessage)
                        is Response.Success -> {
                            repository.getLogRecords(logId)
                                .collect { recordResponse ->
                                    state.value = when(recordResponse) {
                                        is Response.Loading ->
                                            ViewState.Loading
                                        is Response.Failure ->
                                            ViewState.Error(recordResponse.errorMessage)
                                        is Response.Success ->
                                            ViewState.Show(HitchLogState(response.data, recordResponse.data))
                                    }
                                }
                        }
                    }
            }.collect()
        }
    }

    companion object {
        val logger = logging()
    }
}

