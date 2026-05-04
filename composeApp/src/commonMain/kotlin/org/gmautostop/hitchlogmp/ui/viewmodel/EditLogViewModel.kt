package org.gmautostop.hitchlogmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.Repository
import org.gmautostop.hitchlogmp.domain.Response
import org.lighthousegames.logging.logging

data class EditLogState(
    val log: HitchLog? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val showDeleteDialog: Boolean = false,
    val isNewMode: Boolean = true,
    val isSaveEnabled: Boolean = false
)

sealed interface EditLogAction {
    data class OnNameChange(val name: String) : EditLogAction
    data object OnSaveClick : EditLogAction
    data object OnDeleteClick : EditLogAction
    data object OnShowDeleteDialog : EditLogAction
    data object OnDismissDeleteDialog : EditLogAction
}

sealed interface EditLogEvent {
    data object NavigateBack : EditLogEvent
}

class EditLogViewModel(
    private val logId: String,
    private val repository: Repository,
    private val authService: AuthService
): ViewModel() {
    val state: StateFlow<EditLogState>
        field = MutableStateFlow(EditLogState(isNewMode = logId.isEmpty()))

    private val _events = Channel<EditLogEvent>()
    val events: Flow<EditLogEvent>
        field = _events.receiveAsFlow()

    private var watchJob: Job? = null

    init {
        viewModelScope.launch {
            val userId = authService.currentUser.value?.id

            when {
                userId == null -> {
                    state.update { it.copy(isLoading = false, error = AppError.NotAuthenticated) }
                }
                logId.isEmpty() -> {
                    state.update { 
                        it.copy(
                            log = HitchLog(userId = userId),
                            isLoading = false,
                            isNewMode = true
                        )
                    }
                }
                else -> {
                    watchJob = viewModelScope.launch {
                        repository.getLog(logId).distinctUntilChanged().collect { response ->
                            when (response) {
                                is Response.Loading -> {
                                    state.update { it.copy(isLoading = true) }
                                }
                                is Response.Success -> {
                                    state.update { 
                                        it.copy(
                                            log = response.data,
                                            isLoading = false,
                                            isNewMode = false,
                                            isSaveEnabled = response.data.name.trim().isNotEmpty()
                                        )
                                    }
                                }
                                is Response.Failure -> {
                                    logger.e { response.error.displayMessage }
                                    state.update { 
                                        it.copy(
                                            isLoading = false,
                                            error = response.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun onAction(action: EditLogAction) {
        when (action) {
            is EditLogAction.OnNameChange -> updateName(action.name)
            is EditLogAction.OnSaveClick -> saveLog()
            is EditLogAction.OnDeleteClick -> deleteLog()
            is EditLogAction.OnShowDeleteDialog -> {
                state.update { it.copy(showDeleteDialog = true) }
            }
            is EditLogAction.OnDismissDeleteDialog -> {
                state.update { it.copy(showDeleteDialog = false) }
            }
        }
    }

    private fun updateName(name: String) {
        state.value.log?.let { log ->
            state.update { 
                it.copy(
                    log = log.copy(name = name),
                    isSaveEnabled = name.trim().isNotEmpty()
                )
            }
        }
    }

    private fun saveLog() {
        val log = state.value.log ?: return
        
        viewModelScope.launch {
            state.update { it.copy(isLoading = true) }
            
            val flow = when {
                log.id.isEmpty() -> repository.addLog(log)
                else -> repository.updateLog(log)
            }
            
            flow.collect { response ->
                when (response) {
                    is Response.Loading -> Unit
                    is Response.Success -> {
                        _events.send(EditLogEvent.NavigateBack)
                    }
                    is Response.Failure -> {
                        logger.e { response.error.displayMessage }
                        state.update { 
                            it.copy(
                                isLoading = false,
                                error = response.error
                            )
                        }
                    }
                }
            }
        }
    }

    private fun deleteLog() {
        val log = state.value.log ?: return
        
        // Cancel the snapshot listener before deleting so Firestore's local "document
        // doesn't exist" snapshot doesn't race with the delete confirmation.
        watchJob?.cancel()
        
        viewModelScope.launch {
            state.update { it.copy(isLoading = true, showDeleteDialog = false) }
            
            repository.deleteLog(log.id).collect { response ->
                when (response) {
                    is Response.Loading -> Unit
                    is Response.Success -> {
                        _events.send(EditLogEvent.NavigateBack)
                    }
                    is Response.Failure -> {
                        logger.e { response.error.displayMessage }
                        state.update { 
                            it.copy(
                                isLoading = false,
                                error = response.error
                            )
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
