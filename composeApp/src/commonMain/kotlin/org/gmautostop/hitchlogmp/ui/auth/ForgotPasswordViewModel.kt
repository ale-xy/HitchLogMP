package org.gmautostop.hitchlogmp.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.ui.UiText
import org.lighthousegames.logging.logging

data class ForgotPasswordState(
    val email: String = "",
    val emailError: UiText? = null,
    val isLoading: Boolean = false
) {
    val isSubmitEnabled: Boolean
        get() = email.isNotBlank() && !isLoading
}

sealed interface ForgotPasswordAction {
    data class OnEmailChange(val email: String) : ForgotPasswordAction
    data object OnSubmit : ForgotPasswordAction
}

sealed interface ForgotPasswordEvent {
    data class NavigateToForgotPasswordSent(val email: String) : ForgotPasswordEvent
    data class ShowError(val error: UiText) : ForgotPasswordEvent
}

class ForgotPasswordViewModel(
    private val authService: AuthService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val state: StateFlow<ForgotPasswordState>
        field = MutableStateFlow(
            ForgotPasswordState(
                email = savedStateHandle["email"] ?: ""
            )
        )

    private val _events = Channel<ForgotPasswordEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ForgotPasswordAction) {
        when (action) {
            is ForgotPasswordAction.OnEmailChange -> {
                state.update { it.copy(email = action.email, emailError = null) }
                savedStateHandle["email"] = action.email
            }
            ForgotPasswordAction.OnSubmit -> onSubmit()
        }
    }

    private fun onSubmit() {
        val email = state.value.email
        val emailError = validateEmail(email)

        if (emailError != null) {
            state.update { it.copy(emailError = emailError) }
            return
        }

        state.update { it.copy(isLoading = true, emailError = null) }

        viewModelScope.launch {
            try {
                authService.sendPasswordResetEmail(email)
                state.update { it.copy(isLoading = false) }
                _events.send(ForgotPasswordEvent.NavigateToForgotPasswordSent(email))
            } catch (e: Exception) {
                log.e(err = e) { "Password reset email failed" }
                state.update { it.copy(isLoading = false) }
                _events.send(ForgotPasswordEvent.ShowError(e.toAuthErrorUiText()))
            }
        }
    }

    companion object {
        private val log = logging()
    }
}
