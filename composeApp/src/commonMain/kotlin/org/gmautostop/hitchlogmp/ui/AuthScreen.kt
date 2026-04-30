package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.ui.viewmodel.AuthViewModel
import org.lighthousegames.logging.logging

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
) {
    val log = logging("AuthScreen")
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect {
            onAuthenticated()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (uiState.currentUser == null) {
                Button(
                    onClick = { viewModel.onAnonymousLogin() },
                    enabled = !uiState.isLoading,
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp), 
                            strokeWidth = 2.dp
                        )
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
                            log.d { "Google sign-in success: uid=${firebaseUser?.uid}" }
                            
                            firebaseUser?.let {
                                viewModel.onLogin(it)
                            } ?: run {
                                log.e { "FirebaseUser is null despite success result" }
                                scope.launch {
                                    snackbarHostState.showSnackbar("Ошибка входа: пользователь не найден")
                                }
                            }
                        } else {
                            val error = result.exceptionOrNull()
                            log.e { "Google sign-in failed: ${error?.message}" }
                            log.e { "Error type: ${error?.let { it::class.simpleName }}" }
                            log.e { "Stack trace: ${error?.stackTraceToString()}" }
                            
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    "Ошибка входа через Google: ${error?.message ?: "Неизвестная ошибка"}"
                                )
                            }
                        }
                    }
                ) {
                    GoogleSignInButton(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!uiState.isLoading) {
                            this.onClick()
                        }
                    }
                }
            }
        }
    }
}
