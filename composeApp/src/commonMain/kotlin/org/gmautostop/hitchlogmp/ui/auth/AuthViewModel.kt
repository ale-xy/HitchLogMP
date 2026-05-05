package org.gmautostop.hitchlogmp.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.domain.User
import org.gmautostop.hitchlogmp.ui.UiText
import org.lighthousegames.logging.logging

data class AuthState(
    val currentUser: User? = null,
    val isGoogleLoading: Boolean = false,
    val isAppleLoading: Boolean = false,
    val isAnonymousLoading: Boolean = false,
    val showAnonymousWarningDialog: Boolean = false
) {
    val isAuthenticated: Boolean get() = currentUser != null
}

sealed interface AuthAction {
    data object OnEmailLoginClick : AuthAction
    data object OnGoogleLoginClick : AuthAction
    data class OnGoogleLoginResult(val firebaseUser: FirebaseUser?) : AuthAction
    data object OnAppleLoginClick : AuthAction
    data object OnAnonymousLoginClick : AuthAction
    data object OnDismissAnonymousWarningDialog : AuthAction
    data object OnConfirmAnonymousLogin : AuthAction
}

sealed interface AuthEvent {
    data object NavigateToLogList : AuthEvent
    data object NavigateToEmailLogin : AuthEvent
    data class ShowError(val error: UiText) : AuthEvent
}

class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val log = logging()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState>
        field = _state

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    init {
        // Observe current user from AuthService
        viewModelScope.launch {
            authService.currentUser.collect { user ->
                _state.update { it.copy(currentUser = user) }
            }
        }
    }

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.OnEmailLoginClick -> {
                viewModelScope.launch {
                    _events.send(AuthEvent.NavigateToEmailLogin)
                }
            }
            is AuthAction.OnGoogleLoginClick -> {
                _state.update { it.copy(isGoogleLoading = true) }
            }
            is AuthAction.OnGoogleLoginResult -> {
                handleGoogleLoginResult(action.firebaseUser)
            }
            is AuthAction.OnAppleLoginClick -> {
                // TODO: Implement Apple login
//                _state.update { it.copy(isAppleLoading = true) }
            }
            is AuthAction.OnAnonymousLoginClick -> {
                _state.update { it.copy(showAnonymousWarningDialog = true) }
            }
            is AuthAction.OnDismissAnonymousWarningDialog -> {
                _state.update { it.copy(showAnonymousWarningDialog = false) }
            }
            is AuthAction.OnConfirmAnonymousLogin -> {
                _state.update { it.copy(showAnonymousWarningDialog = false) }
                handleAnonymousLogin()
            }
        }
    }

    private fun handleGoogleLoginResult(firebaseUser: FirebaseUser?) {
        viewModelScope.launch {
            try {
                if (firebaseUser == null) {
                    _state.update { it.copy(isGoogleLoading = false) }
                    _events.send(AuthEvent.ShowError(UiText.DynamicString("Google login cancelled")))
                    return@launch
                }

                // Wait for AuthService to process the user
                withTimeout(5000) {
                    authService.currentUser.collect { user ->
                        if (user != null) {
                            _state.update { it.copy(isGoogleLoading = false) }
                            _events.send(AuthEvent.NavigateToLogList)
                            return@collect
                        }
                    }
                }
            } catch (e: Exception) {
                log.e(err = e) { "Google login failed" }
                _state.update { it.copy(isGoogleLoading = false) }
                _events.send(AuthEvent.ShowError(e.toAuthErrorUiText()))
            }
        }
    }

    private fun handleAnonymousLogin() {
        _state.update { it.copy(isAnonymousLoading = true) }
        viewModelScope.launch {
            try {
                authService.signInAnonymously()
                _state.update { it.copy(isAnonymousLoading = false) }
                _events.send(AuthEvent.NavigateToLogList)
            } catch (e: Exception) {
                log.e(err = e) { "Anonymous login failed" }
                _state.update { it.copy(isAnonymousLoading = false) }
                _events.send(AuthEvent.ShowError(e.toAuthErrorUiText()))
            }
        }
    }
}
