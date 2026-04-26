package org.gmautostop.hitchlogmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.lighthousegames.logging.logging

class EditLogViewModel(
    private val logId: String,
    private val repository: Repository,
    private val authService: AuthService
): ViewModel() {
    val state: StateFlow<ViewState<HitchLog>>
        field = MutableStateFlow<ViewState<HitchLog>>(ViewState.Loading)
    private var name: String = ""
    private var watchJob: Job? = null

    private val _navigationEvent = Channel<Unit>(Channel.CONFLATED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            val userId = authService.currentUser.value?.id

            when {
                userId == null -> state.value = ViewState.Error(AppError.NotAuthenticated)
                logId.isEmpty() -> state.value = ViewState.Show(HitchLog(userId = userId))
                else -> {
                    watchJob = viewModelScope.launch {
                        repository.getLog(logId).distinctUntilChanged().collect { response ->
                            state.value = when (response) {
                                is Response.Loading -> ViewState.Loading
                                is Response.Success -> ViewState.Show(response.data).also {
                                    name = response.data.name
                                }
                                is Response.Failure -> ViewState.Error(response.error).also {
                                    logger.e { response.error.displayMessage }
                                }
                            }
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
            viewModelScope.launch {
                state.value = ViewState.Loading
                val flow = when {
                    log.id.isEmpty() -> repository.addLog(log.copy(name = name))
                    else -> repository.updateLog(log.copy(name = name))
                }
                flow.collect { response ->
                    when (response) {
                        is Response.Loading -> Unit
                        is Response.Success -> _navigationEvent.send(Unit)
                        is Response.Failure -> {
                            logger.e { response.error.displayMessage }
                            state.value = ViewState.Error(response.error)
                        }
                    }
                }
            }
        }
    }

    fun deleteLog() {
        withLog { log ->
            // Cancel the snapshot listener before deleting so Firestore's local "document
            // doesn't exist" snapshot doesn't race with the delete confirmation.
            watchJob?.cancel()
            viewModelScope.launch {
                state.value = ViewState.Loading
                repository.deleteLog(log.id).collect { response ->
                    when (response) {
                        is Response.Loading -> Unit
                        is Response.Success -> _navigationEvent.send(Unit)
                        is Response.Failure -> {
                            logger.e { response.error.displayMessage }
                            state.value = ViewState.Error(response.error)
                        }
                    }
                }
            }
        }
    }

    companion object {
        val logger = logging()
    }
}
