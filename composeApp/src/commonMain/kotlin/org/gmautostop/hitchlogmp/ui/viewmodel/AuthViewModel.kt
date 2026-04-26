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
        log.d { "onLogin uid ${firebaseUser.uid} anon ${firebaseUser.isAnonymous}" }
        _navigationEvent.trySend(Unit)
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
