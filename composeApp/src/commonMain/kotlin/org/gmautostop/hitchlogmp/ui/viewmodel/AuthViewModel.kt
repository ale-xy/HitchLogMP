package org.gmautostop.hitchlogmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.domain.User
import org.lighthousegames.logging.logging

/**
 * Represents which authentication button is currently loading.
 */
enum class AuthButtonLoading {
    EMAIL,
    GOOGLE,
    APPLE,
    ANONYMOUS
}

data class AuthState(
    val currentUser: User? = null,
    val loadingButton: AuthButtonLoading? = null,
) {
    val isAuthenticated: Boolean get() = currentUser != null
}

sealed interface AuthAction {
    data object OnEmailLoginClick : AuthAction
    data object OnGoogleLoginClick : AuthAction
    data class OnGoogleLoginResult(val firebaseUser: FirebaseUser?) : AuthAction
    data object OnAppleLoginClick : AuthAction
    data object OnAnonymousLoginClick : AuthAction
}

sealed interface AuthEvent {
    data object NavigateToLogList : AuthEvent
    data class ShowError(val message: String) : AuthEvent
}

class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _loadingButton = MutableStateFlow<AuthButtonLoading?>(null)

    val state: StateFlow<AuthState> = combine(
        authService.currentUser,
        _loadingButton
    ) { user, loadingButton ->
        AuthState(currentUser = user, loadingButton = loadingButton)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AuthState(currentUser = authService.currentUser.value)
    )

    private val _events = Channel<AuthEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.OnEmailLoginClick -> {
                // TODO: Implement email login
            }
            is AuthAction.OnGoogleLoginClick -> {
                _loadingButton.value = AuthButtonLoading.GOOGLE
            }
            is AuthAction.OnGoogleLoginResult -> {
                handleGoogleLoginResult(action.firebaseUser)
            }
            is AuthAction.OnAppleLoginClick -> {
                // TODO: Implement Apple login
            }
            is AuthAction.OnAnonymousLoginClick -> {
                handleAnonymousLogin()
            }
        }
    }

    private fun handleGoogleLoginResult(firebaseUser: FirebaseUser?) {
        viewModelScope.launch {
            try {
                if (firebaseUser == null) {
                    log.e { "FirebaseUser is null despite success result" }
                    _events.send(AuthEvent.ShowError("Пользователь не найден"))
                    _loadingButton.value = null
                    return@launch
                }

                log.d { "Google sign-in success: uid=${firebaseUser.uid}" }
                
                // Refresh auth state to ensure it propagates
                authService.refreshAuthState()
                
                // Wait for currentUser flow to update with the new user (10 second timeout)
                withTimeout(10_000) {
                    val user = authService.currentUser.first { user ->
                        user != null && user.id == firebaseUser.uid
                    }
                    log.d { "Auth state confirmed: userId=${user?.id}, isAnonymous=${user?.isAnonymous}" }
                }
                
                _events.send(AuthEvent.NavigateToLogList)
            } catch (e: Exception) {
                log.e { "Google login failed: ${e.message}" }
                _events.send(AuthEvent.ShowError(e.message ?: "Неизвестная ошибка"))
            } finally {
                _loadingButton.value = null
            }
        }
    }

    private fun handleAnonymousLogin() {
        viewModelScope.launch {
            _loadingButton.value = AuthButtonLoading.ANONYMOUS
            try {
                authService.signInAnonymously()
                val user = authService.currentUser.first { user ->
                    user?.isAnonymous == true
                }
                log.d { "onAnonymousLogin currentUser ${user?.id} anon ${user?.isAnonymous}" }
                _events.send(AuthEvent.NavigateToLogList)
            } catch (e: Exception) {
                log.e { "Anonymous login failed: ${e.message}" }
                _events.send(AuthEvent.ShowError("Ошибка анонимного входа"))
            } finally {
                _loadingButton.value = null
            }
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            authService.signOut()
            log.d { "onSignOut" }
        }
    }

    companion object {
        val log = logging()
    }
}
