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

data class EmailLoginState(
    val email: String = "",
    val password: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val isLoading: Boolean = false
) {
    val isSubmitEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !isLoading
}

sealed interface EmailLoginAction {
    data class OnEmailChange(val email: String) : EmailLoginAction
    data class OnPasswordChange(val password: String) : EmailLoginAction
    data object OnSubmit : EmailLoginAction
    data object OnNavigateToRegister : EmailLoginAction
    data object OnNavigateToForgotPassword : EmailLoginAction
}

sealed interface EmailLoginEvent {
    data object NavigateToLogList : EmailLoginEvent
    data object NavigateToRegister : EmailLoginEvent
    data object NavigateToForgotPassword : EmailLoginEvent
    data class ShowError(val error: UiText) : EmailLoginEvent
}

class EmailLoginViewModel(
    private val authService: AuthService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val state: StateFlow<EmailLoginState>
        field = MutableStateFlow(
            EmailLoginState(
                email = savedStateHandle["email"] ?: "",
                password = savedStateHandle["password"] ?: ""
            )
        )

    private val _events = Channel<EmailLoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: EmailLoginAction) {
        when (action) {
            is EmailLoginAction.OnEmailChange -> {
                state.update { it.copy(email = action.email, emailError = null) }
                savedStateHandle["email"] = action.email
            }
            is EmailLoginAction.OnPasswordChange -> {
                state.update { it.copy(password = action.password, passwordError = null) }
                savedStateHandle["password"] = action.password
            }
            EmailLoginAction.OnSubmit -> onSubmit()
            EmailLoginAction.OnNavigateToRegister -> {
                viewModelScope.launch {
                    _events.send(EmailLoginEvent.NavigateToRegister)
                }
            }
            EmailLoginAction.OnNavigateToForgotPassword -> {
                viewModelScope.launch {
                    _events.send(EmailLoginEvent.NavigateToForgotPassword)
                }
            }
        }
    }

    private fun onSubmit() {
        val email = state.value.email
        val password = state.value.password

        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)

        if (emailError != null || passwordError != null) {
            state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        state.update { it.copy(isLoading = true, emailError = null, passwordError = null) }

        viewModelScope.launch {
            try {
                authService.signInWithEmailAndPassword(email, password)
                state.update { it.copy(isLoading = false) }
                _events.send(EmailLoginEvent.NavigateToLogList)
            } catch (e: Exception) {
                state.update { it.copy(isLoading = false) }
                _events.send(EmailLoginEvent.ShowError(e.toAuthErrorUiText()))
            }
        }
    }
}
