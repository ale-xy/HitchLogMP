package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import org.gmautostop.hitchlogmp.domain.User
import org.gmautostop.hitchlogmp.ui.viewmodel.AuthViewModel
import org.lighthousegames.logging.logging

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: (String) -> Unit,
) {
    val log = logging("AuthScreen")

    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthenticated by viewModel.isAuthenticated.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current

//    DisposableEffect(lifecycleOwner) {
//        val observer = LifecycleEventObserver { _, event ->
//            if (event == Lifecycle.Event.ON_START) {
//                log.d { "DisposableEffect onStart" }
//                viewModel.onStart()
//            }
//        }
//
//        // Add the observer to the lifecycle
//        lifecycleOwner.lifecycle.addObserver(observer)
//
//        // When the effect leaves the Composition, remove the observer
//        onDispose {
//            lifecycleOwner.lifecycle.removeObserver(observer)
//        }
//    }

    LaunchedEffect(isAuthenticated) {
        snapshotFlow { isAuthenticated }.collect {
            log.d { "LaunchedEffect isAuthenticated $it" }

            if (it) {
                currentUser?.let { user -> onAuthenticated(user.id) }
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (currentUser == null) {
            Button(onClick = {
                viewModel.onAnonymousLogin()
            }) {
                Text("Анонимус")
            }
        }

        if (currentUser == null || currentUser?.isAnonymous == true) {
            GoogleButtonUiContainerFirebase(
                modifier = Modifier,
                linkAccount = true,
                onResult = { result ->
                    if (result.isSuccess) {
                        val user = result.getOrNull()

                        log.d { "onResult $user" }

                        user?.let {
                            viewModel.onLogin(User(it.uid, it.isAnonymous))
                        }
                    } else {
                        //todo error
                        log.e { "Google sign in failed: ${result.exceptionOrNull()?.message}"}
                    }
                }
            ) {
                GoogleSignInButton(modifier = Modifier.fillMaxWidth()) {
                    this.onClick()
                }
            }
        }

        if (currentUser != null) {
            Button(onClick = {
                viewModel.onSignOut()
            }) {
                Text("Выход")
            }
        }
    }
}

