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

data class EmailRegisterState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val emailError: UiText? = null,
    val passwordError: UiText? = null,
    val confirmPasswordError: UiText? = null,
    val isLoading: Boolean = false
) {
    val isSubmitEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && !isLoading
}

sealed interface EmailRegisterAction {
    data class OnEmailChange(val email: String) : EmailRegisterAction
    data class OnPasswordChange(val password: String) : EmailRegisterAction
    data class OnConfirmPasswordChange(val confirmPassword: String) : EmailRegisterAction
    data object OnSubmit : EmailRegisterAction
    data object OnNavigateToLogin : EmailRegisterAction
}

sealed interface EmailRegisterEvent {
    data object NavigateToLogList : EmailRegisterEvent
    data object NavigateToLogin : EmailRegisterEvent
    data class ShowError(val error: UiText) : EmailRegisterEvent
    data object ShowEmailVerificationSent : EmailRegisterEvent
}

class EmailRegisterViewModel(
    private val authService: AuthService,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    val state: StateFlow<EmailRegisterState>
        field = MutableStateFlow(
            EmailRegisterState(
                email = savedStateHandle["email"] ?: "",
                password = savedStateHandle["password"] ?: "",
                confirmPassword = savedStateHandle["confirmPassword"] ?: ""
            )
        )

    private val _events = Channel<EmailRegisterEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: EmailRegisterAction) {
        when (action) {
            is EmailRegisterAction.OnEmailChange -> {
                state.update { it.copy(email = action.email, emailError = null) }
                savedStateHandle["email"] = action.email
            }
            is EmailRegisterAction.OnPasswordChange -> {
                state.update { it.copy(password = action.password, passwordError = null) }
                savedStateHandle["password"] = action.password
            }
            is EmailRegisterAction.OnConfirmPasswordChange -> {
                state.update { it.copy(confirmPassword = action.confirmPassword, confirmPasswordError = null) }
                savedStateHandle["confirmPassword"] = action.confirmPassword
            }
            EmailRegisterAction.OnSubmit -> onSubmit()
            EmailRegisterAction.OnNavigateToLogin -> {
                viewModelScope.launch {
                    _events.send(EmailRegisterEvent.NavigateToLogin)
                }
            }
        }
    }

    private fun onSubmit() {
        val email = state.value.email
        val password = state.value.password
        val confirmPassword = state.value.confirmPassword

        val emailError = validateEmail(email)
        val passwordError = validatePassword(password)
        val confirmPasswordError = validatePasswordsMatch(password, confirmPassword)

        if (emailError != null || passwordError != null || confirmPasswordError != null) {
            state.update {
                it.copy(
                    emailError = emailError,
                    passwordError = passwordError,
                    confirmPasswordError = confirmPasswordError
                )
            }
            return
        }

        state.update { it.copy(isLoading = true, emailError = null, passwordError = null, confirmPasswordError = null) }

        viewModelScope.launch {
            try {
                authService.createUserWithEmailAndPassword(email, password)
                authService.sendEmailVerification()
                state.update { it.copy(isLoading = false) }
                _events.send(EmailRegisterEvent.ShowEmailVerificationSent)
                _events.send(EmailRegisterEvent.NavigateToLogList)
            } catch (e: Exception) {
                log.e(err = e) { "Email registration failed" }
                state.update { it.copy(isLoading = false) }
                _events.send(EmailRegisterEvent.ShowError(e.toAuthErrorUiText()))
            }
        }
    }

    companion object {
        private val log = logging()
    }
}
