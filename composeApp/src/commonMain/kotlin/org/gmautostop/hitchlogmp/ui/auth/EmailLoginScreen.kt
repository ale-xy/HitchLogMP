package org.gmautostop.hitchlogmp.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.back_button
import hitchlogmp.composeapp.generated.resources.email_label
import hitchlogmp.composeapp.generated.resources.email_login_title
import hitchlogmp.composeapp.generated.resources.error_empty_email
import hitchlogmp.composeapp.generated.resources.error_empty_password
import hitchlogmp.composeapp.generated.resources.forgot_password
import hitchlogmp.composeapp.generated.resources.login_button
import hitchlogmp.composeapp.generated.resources.logging_in
import hitchlogmp.composeapp.generated.resources.no_account
import hitchlogmp.composeapp.generated.resources.password_label
import hitchlogmp.composeapp.generated.resources.password_visibility_toggle
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.ui.ObserveAsEvents
import org.gmautostop.hitchlogmp.ui.asString
import org.gmautostop.hitchlogmp.ui.asStringSuspend
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLButton
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.hlFilledTextFieldColors
import org.gmautostop.hitchlogmp.ui.auth.EmailLoginAction
import org.gmautostop.hitchlogmp.ui.auth.EmailLoginEvent
import org.gmautostop.hitchlogmp.ui.auth.EmailLoginState
import org.gmautostop.hitchlogmp.ui.auth.EmailLoginViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable - holds ViewModel and observes events.
 */
@Composable
fun EmailLoginScreen(
    onAuthenticated: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    viewModel: EmailLoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is EmailLoginEvent.NavigateToLogList -> onAuthenticated()
            is EmailLoginEvent.NavigateToRegister -> onNavigateToRegister()
            is EmailLoginEvent.NavigateToForgotPassword -> onNavigateToForgotPassword()
            is EmailLoginEvent.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.error.asStringSuspend())
                }
            }
        }
    }

    EmailLoginScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Stateless screen composable - pure state + onAction.
 */
@Composable
fun EmailLoginScreen(
    state: EmailLoginState,
    onAction: (EmailLoginAction) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = HLColors.Background,
        topBar = {
            HLTopBar(
                title = stringResource(Res.string.email_login_title),
                onNavigateUp = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val emailFocusRequester = remember { FocusRequester() }
        var passwordVisible by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Email field
            TextField(
                value = state.email,
                onValueChange = { onAction(EmailLoginAction.OnEmailChange(it)) },
                label = { Text(stringResource(Res.string.email_label)) },
                supportingText = state.emailError?.let { { Text(it.asString()) } },
                isError = state.emailError != null,
                colors = hlFilledTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester)
            )

            LaunchedEffect(Unit) {
                emailFocusRequester.requestFocus()
            }

            // Password field
            TextField(
                value = state.password,
                onValueChange = { onAction(EmailLoginAction.OnPasswordChange(it)) },
                label = { Text(stringResource(Res.string.password_label)) },
                supportingText = state.passwordError?.let { { Text(it.asString()) } },
                isError = state.passwordError != null,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = stringResource(Res.string.password_visibility_toggle),
                            tint = HLColors.OnSurfaceVariant
                        )
                    }
                },
                colors = hlFilledTextFieldColors(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Forgot password button
            TextButton(
                onClick = { onAction(EmailLoginAction.OnNavigateToForgotPassword) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = stringResource(Res.string.forgot_password),
                    style = HLTypography.labelLarge,
                    color = HLColors.Primary
                )
            }

            Spacer(Modifier.height(8.dp))

            // Login button
            HLButton(
                onClick = { onAction(EmailLoginAction.OnSubmit) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = HLColors.OnPrimary
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.login_button),
                        style = HLTypography.labelLarge
                    )
                }
            }

            // Register link
            TextButton(
                onClick = { onAction(EmailLoginAction.OnNavigateToRegister) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(Res.string.no_account),
                    style = HLTypography.labelLarge,
                    color = HLColors.Primary
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun EmailLoginScreenPreview() {
    HLTheme {
        EmailLoginScreen(
            state = EmailLoginState(
                email = "user@example.com",
                password = "password123"
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun EmailLoginScreenWithErrorsPreview() {
    HLTheme {
        EmailLoginScreen(
            state = EmailLoginState(
                email = "invalid",
                password = "123",
                emailError = org.gmautostop.hitchlogmp.ui.UiText.DynamicString("Invalid email"),
                passwordError = org.gmautostop.hitchlogmp.ui.UiText.DynamicString("Password too short")
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun EmailLoginScreenLoadingPreview() {
    HLTheme {
        EmailLoginScreen(
            state = EmailLoginState(
                email = "user@example.com",
                password = "password123",
                isLoading = true
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
