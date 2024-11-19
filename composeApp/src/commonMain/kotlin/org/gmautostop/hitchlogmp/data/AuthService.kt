package org.gmautostop.hitchlogmp.data

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.gmautostop.hitchlogmp.domain.User

class AuthService(
    val auth: FirebaseAuth,
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    val currentUserId: String
        get() = auth.currentUser?.uid.toString()

    val isAuthenticated: Boolean
        get() = auth.currentUser != null

    val currentUser: StateFlow<User?> =
        auth.authStateChanged.map {
            it?.toUser()
        }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = auth.currentUser?.toUser()
        )

    private suspend fun launchWithAwait(block : suspend  () -> Unit) {
        scope.async {
            block()
        }.await()
    }

    suspend fun authenticate(email: String, password: String) {
        launchWithAwait {
            auth.signInWithEmailAndPassword(email, password)
        }
    }

    suspend fun createUser(email: String, password: String) {
        val result = launchWithAwait {
            auth.createUserWithEmailAndPassword(email, password)
        }
    }

    suspend fun signInAnonymously() {
        launchWithAwait {
            val result = auth.signInAnonymously()
        }
    }

    suspend fun signOut() {
        if (auth.currentUser?.isAnonymous == true) {
            auth.currentUser?.delete()
        }

        auth.signOut()
    }
}

fun FirebaseUser.toUser() = User(uid, isAnonymous)