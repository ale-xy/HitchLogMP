package org.gmautostop.hitchlogmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.data.HitchLog
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.gmautostop.hitchlogmp.ui.viewmodel.LogListViewModel.Companion.logger
import org.lighthousegames.logging.logging

class EditLogViewModel(
    //savedStateHandle: SavedStateHandle,
    private val logId: String,
    private val repository: Repository
): ViewModel() {
    val state = MutableStateFlow<ViewState<HitchLog>>(ViewState.Loading)
    private var name: String = ""

    init {
        viewModelScope.launch {
            val userId = repository.userId()

            when {
                userId == null -> state.value = ViewState.Error("Not logged in!")
                logId.isEmpty() -> state.value = ViewState.Show(HitchLog(userId = userId))
                else -> repository.getLog(logId).distinctUntilChanged().collect { response ->
                    state.value = when (response) {
                        is Response.Loading -> ViewState.Loading
                        is Response.Success -> ViewState.Show(response.data).also {
                            name = response.data.name
                        }
                        is Response.Failure -> ViewState.Error(response.errorMessage).also {
                            logger.e { response.errorMessage }
                        }
                    }
                }
            }
        }
    }

    private fun withLog(action: (HitchLog) -> Unit) {
        val log = (state.value as? ViewState.Show<HitchLog>)?.value
        log?.let { action(it) }
    }

    fun updateName(value: String) {
        withLog {
            name = value
            state.value = ViewState.Show(it.copy(name = value))
        }
    }

    fun saveLog() {
        withLog { log ->
            when {
                log.id.isEmpty() ->
                    repository.userId().let {
                        repository.addLog(log.copy(name = name)).launchIn(viewModelScope)
                    }
                else -> repository.updateLog(log.copy(name = name)).launchIn(viewModelScope)
            }
        }
    }

    fun deleteLog() {
        withLog {
            repository.deleteLog(it.id).launchIn(viewModelScope)
        }
    }

    companion object {
        val logger = logging()
    }
}
