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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.email_label
import hitchlogmp.composeapp.generated.resources.forgot_password_description
import hitchlogmp.composeapp.generated.resources.forgot_password_title
import hitchlogmp.composeapp.generated.resources.send_reset_link
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable - holds ViewModel and observes events.
 */
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onEmailSent: (String) -> Unit,
    viewModel: ForgotPasswordViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ForgotPasswordEvent.NavigateToForgotPasswordSent -> onEmailSent(event.email)
            is ForgotPasswordEvent.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.error.asStringSuspend())
                }
            }
        }
    }

    ForgotPasswordScreen(
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
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    onAction: (ForgotPasswordAction) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = HLColors.Background,
        topBar = {
            HLTopBar(
                title = stringResource(Res.string.forgot_password_title),
                onNavigateUp = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Description text
            Text(
                text = stringResource(Res.string.forgot_password_description),
                style = HLTypography.bodyMedium,
                color = HLColors.OnSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Email field
            val emailFocusRequester = remember { FocusRequester() }

            TextField(
                value = state.email,
                onValueChange = { onAction(ForgotPasswordAction.OnEmailChange(it)) },
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

            Spacer(Modifier.height(8.dp))

            // Send reset link button
            HLButton(
                onClick = { onAction(ForgotPasswordAction.OnSubmit) },
                enabled = state.isSubmitEnabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = HLColors.OnPrimary
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.send_reset_link),
                        style = HLTypography.labelLarge
                    )
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ForgotPasswordScreenPreview() {
    HLTheme {
        ForgotPasswordScreen(
            state = ForgotPasswordState(
                email = "user@example.com"
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun ForgotPasswordScreenWithErrorPreview() {
    HLTheme {
        ForgotPasswordScreen(
            state = ForgotPasswordState(
                email = "invalid",
                emailError = org.gmautostop.hitchlogmp.ui.UiText.DynamicString("Invalid email")
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun ForgotPasswordScreenLoadingPreview() {
    HLTheme {
        ForgotPasswordScreen(
            state = ForgotPasswordState(
                email = "user@example.com",
                isLoading = true
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
