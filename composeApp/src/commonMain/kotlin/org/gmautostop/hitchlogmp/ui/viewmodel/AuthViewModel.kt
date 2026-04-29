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

data class AuthUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
) {
    val isAuthenticated: Boolean get() = currentUser != null
}

class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)

    val uiState: StateFlow<AuthUiState> = combine(
        authService.currentUser,
        _isLoading
    ) { user, isLoading ->
        AuthUiState(currentUser = user, isLoading = isLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AuthUiState(currentUser = authService.currentUser.value)
    )

    private val _navigationEvent = Channel<Unit>(Channel.CONFLATED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    fun onAnonymousLogin() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                authService.signInAnonymously()
                // todo error
                val user = authService.currentUser.first { user ->
                    user?.isAnonymous == true
                }
                log.d { "onAnonymousLogin currentUser ${user?.id} anon ${user?.isAnonymous}" }
                _navigationEvent.trySend(Unit)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onLogin(firebaseUser: FirebaseUser) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                log.d { "onLogin started: uid=${firebaseUser.uid}, isAnonymous=${firebaseUser.isAnonymous}" }
                
                // Refresh auth state to ensure it propagates
                authService.refreshAuthState()
                
                // Wait for currentUser flow to update with the new user (10 second timeout)
                withTimeout(10_000) {
                    val user = authService.currentUser.first { user ->
                        user != null && user.id == firebaseUser.uid
                    }
                    log.d { "Auth state confirmed: userId=${user?.id}, isAnonymous=${user?.isAnonymous}" }
                }
                
                _navigationEvent.trySend(Unit)
            } catch (e: Exception) {
                log.e { "Login failed: ${e.message}" }
                log.e { "Stack trace: ${e.stackTraceToString()}" }
                // Error will be visible in logs, user stays on auth screen
            } finally {
                _isLoading.value = false
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
