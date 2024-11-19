package org.gmautostop.hitchlogmp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.data.User
import org.lighthousegames.logging.logging

class AuthViewModel(
    private val authService: AuthService
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    init {
        viewModelScope.launch {
            _isAuthenticated.value = false

            authService.currentUser.collect {
                log.d { "init currentUser ${it?.id} anon ${it?.isAnonymous} currentUserId ${authService.currentUserId}" }
                _currentUser.value = it
                _isAuthenticated.value = it != null
            }
        }
    }

    fun onAnonymousLogin() {
        viewModelScope.launch {
            authService.signInAnonymously()

            // todo error
            val user = authService.currentUser.first { user ->
                user?.isAnonymous == true
            }
            log.d { "onAnonymousLogin currentUser ${user?.id} anon ${user?.isAnonymous}" }

            _isAuthenticated.value = true
        }
    }

    fun onLogin(user: User) {
        _currentUser.value = user
        log.d { "onLogin currentUser ${user.id} anon ${user.isAnonymous}" }

        _isAuthenticated.value = true
    }

    fun onSignOut() {
        viewModelScope.launch {
            authService.signOut()
            log.d { "onSignOut" }
        }
    }

//    fun onStart() {
////        _isAuthenticated.value = false
//    }

    companion object {
        val log = logging()
    }

}
