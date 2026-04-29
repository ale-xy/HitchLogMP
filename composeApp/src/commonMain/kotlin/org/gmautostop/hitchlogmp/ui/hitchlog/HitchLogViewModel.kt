package org.gmautostop.hitchlogmp.ui.hitchlog

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
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.gmautostop.hitchlogmp.domain.computeLiveState
import org.gmautostop.hitchlogmp.domain.computeRestMinutes
import org.gmautostop.hitchlogmp.domain.nextActionLadder
import org.lighthousegames.logging.logging

@OptIn(ExperimentalCoroutinesApi::class)
class HitchLogViewModel(
    private val logId: String,
    repository: Repository
) : ViewModel() {
    val state: StateFlow<org.gmautostop.hitchlogmp.ui.viewmodel.ViewState<HitchLogState>>
        field = MutableStateFlow<org.gmautostop.hitchlogmp.ui.viewmodel.ViewState<HitchLogState>>(_root_ide_package_.org.gmautostop.hitchlogmp.ui.viewmodel.ViewState.Loading)

    init {
        viewModelScope.launch {
            repository.getLog(logId)
                .distinctUntilChanged()
                .flatMapLatest { logResponse ->
                    when (logResponse) {
                        is Response.Loading -> flowOf(_root_ide_package_.org.gmautostop.hitchlogmp.ui.viewmodel.ViewState.Loading)
                        is Response.Failure -> flowOf(_root_ide_package_.org.gmautostop.hitchlogmp.ui.viewmodel.ViewState.Error(logResponse.error))
                        is Response.Success -> repository.getLogRecords(logId).map { recordResponse ->
                            when (recordResponse) {
                                is Response.Loading -> _root_ide_package_.org.gmautostop.hitchlogmp.ui.viewmodel.ViewState.Loading
                                is Response.Failure -> _root_ide_package_.org.gmautostop.hitchlogmp.ui.viewmodel.ViewState.Error(recordResponse.error)
                                is Response.Success -> {
                                    val records = recordResponse.data
                                    _root_ide_package_.org.gmautostop.hitchlogmp.ui.viewmodel.ViewState.Show(
                                        HitchLogState(
                                            logName = logResponse.data.name,
                                            teamId = logResponse.data.teamId,
                                            records = records,
                                            summary = SummaryCardState(
                                                lifts = records.count { it.type == HitchLogRecordType.LIFT },
                                                checkpoints = records.count { it.type == HitchLogRecordType.CHECKPOINT },
                                                restMin = computeRestMinutes(records),
                                                liveState = computeLiveState(records)
                                            ),
                                            ladder = nextActionLadder(records)
                                        )
                                    )
                                }
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
