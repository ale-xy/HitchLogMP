package org.gmautostop.hitchlogmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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

@OptIn(ExperimentalCoroutinesApi::class)
class HitchLogViewModel(
    private val logId: String,
    repository: Repository
) : ViewModel() {
    val state: StateFlow<ViewState<HitchLogState>>
        field = MutableStateFlow<ViewState<HitchLogState>>(ViewState.Loading)

    init {
        viewModelScope.launch {
            repository.getLog(logId)
                .distinctUntilChanged()
                .flatMapLatest { logResponse ->
                    when (logResponse) {
                        is Response.Loading -> flowOf(ViewState.Loading)
                        is Response.Failure -> flowOf(ViewState.Error(logResponse.errorMessage))
                        is Response.Success -> repository.getLogRecords(logId).map { recordResponse ->
                            when (recordResponse) {
                                is Response.Loading -> ViewState.Loading
                                is Response.Failure -> ViewState.Error(recordResponse.errorMessage)
                                is Response.Success -> ViewState.Show(
                                    HitchLogState(logResponse.data, recordResponse.data)
                                )
                            }
                        }
                    }
                }
                .collect { state.value = it }
        }
    }

    companion object {
        val logger = logging()
    }
}
