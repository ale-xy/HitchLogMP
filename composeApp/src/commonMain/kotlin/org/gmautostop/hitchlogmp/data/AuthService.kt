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
import org.lighthousegames.logging.logging

class AuthService(
    val auth: FirebaseAuth,
    val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) {

    private val log = logging("AuthService")

    val currentUserId: String
        get() = auth.currentUser?.uid.toString()

    val isAuthenticated: Boolean
        get() = auth.currentUser != null

    val currentUser: StateFlow<User?> =
        auth.authStateChanged.map {
            log.d { "Auth state changed: uid=${it?.uid}, isAnonymous=${it?.isAnonymous}" }
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

    suspend fun signInAnonymously() {
        launchWithAwait {
            auth.signInAnonymously()
        }
    }

    suspend fun refreshAuthState() {
        launchWithAwait {
            auth.currentUser?.reload()
            log.d { "Auth state refreshed: uid=${auth.currentUser?.uid}" }
        }
    }

    suspend fun signOut() {
        if (auth.currentUser?.isAnonymous == true) {
            auth.currentUser?.delete()
        }

        auth.signOut()
    }

    suspend fun signInWithEmailAndPassword(email: String, password: String) {
        launchWithAwait {
            auth.signInWithEmailAndPassword(email, password)
        }
    }

    suspend fun createUserWithEmailAndPassword(email: String, password: String) {
        launchWithAwait {
            auth.createUserWithEmailAndPassword(email, password)
        }
    }

    suspend fun sendPasswordResetEmail(email: String) {
        launchWithAwait {
            auth.sendPasswordResetEmail(email)
        }
    }

    suspend fun sendEmailVerification() {
        launchWithAwait {
            auth.currentUser?.sendEmailVerification()
        }
    }
}

fun FirebaseUser.toUser() = User(uid, isAnonymous)