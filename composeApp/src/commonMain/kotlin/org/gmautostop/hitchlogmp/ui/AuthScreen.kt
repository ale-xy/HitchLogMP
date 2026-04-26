package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import org.gmautostop.hitchlogmp.ui.viewmodel.AuthViewModel
import org.lighthousegames.logging.logging

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
) {
    val log = logging("AuthScreen")

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            onAuthenticated()
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (uiState.currentUser == null) {
            Button(
                onClick = { viewModel.onAnonymousLogin() },
                enabled = !uiState.isLoading,
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Анонимус")
                }
            }
        }

        if (uiState.currentUser == null || uiState.currentUser?.isAnonymous == true) {
            GoogleButtonUiContainerFirebase(
                modifier = Modifier,
                linkAccount = true,
                onResult = { result ->
                    if (result.isSuccess) {
                        val firebaseUser = result.getOrNull()

                        log.d { "onResult $firebaseUser" }

                        firebaseUser?.let {
                            viewModel.onLogin(it)
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

    }
}
