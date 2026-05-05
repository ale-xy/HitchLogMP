package org.gmautostop.hitchlogmp.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.confirm_password_label
import hitchlogmp.composeapp.generated.resources.create_account_button
import hitchlogmp.composeapp.generated.resources.email_label
import hitchlogmp.composeapp.generated.resources.email_verification_sent
import hitchlogmp.composeapp.generated.resources.have_account
import hitchlogmp.composeapp.generated.resources.password_hint
import hitchlogmp.composeapp.generated.resources.password_label
import hitchlogmp.composeapp.generated.resources.password_visibility_toggle
import hitchlogmp.composeapp.generated.resources.register_title
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.ui.ObserveAsEvents
import org.gmautostop.hitchlogmp.ui.UiText
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
fun EmailRegisterScreen(
    onAuthenticated: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: EmailRegisterViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val emailVerificationSentMessage = stringResource(Res.string.email_verification_sent)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is EmailRegisterEvent.NavigateToLogList -> onAuthenticated()
            is EmailRegisterEvent.NavigateToLogin -> onNavigateToLogin()
            is EmailRegisterEvent.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.error.asStringSuspend())
                }
            }
            is EmailRegisterEvent.ShowEmailVerificationSent -> {
                scope.launch {
                    snackbarHostState.showSnackbar(emailVerificationSentMessage)
                }
            }
        }
    }

    EmailRegisterScreen(
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
fun EmailRegisterScreen(
    state: EmailRegisterState,
    onAction: (EmailRegisterAction) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = HLColors.Background,
        topBar = {
            HLTopBar(
                title = stringResource(Res.string.register_title),
                onNavigateUp = onNavigateBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val emailFocusRequester = remember { FocusRequester() }
        var passwordVisible by remember { mutableStateOf(false) }
        var confirmPasswordVisible by remember { mutableStateOf(false) }

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
                onValueChange = { onAction(EmailRegisterAction.OnEmailChange(it)) },
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
                onValueChange = { onAction(EmailRegisterAction.OnPasswordChange(it)) },
                label = { Text(stringResource(Res.string.password_label)) },
                supportingText = if (state.passwordError != null) {
                    { Text(state.passwordError.asString()) }
                } else {
                    { Text(stringResource(Res.string.password_hint)) }
                },
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

            // Confirm password field
            TextField(
                value = state.confirmPassword,
                onValueChange = { onAction(EmailRegisterAction.OnConfirmPasswordChange(it)) },
                label = { Text(stringResource(Res.string.confirm_password_label)) },
                supportingText = state.confirmPasswordError?.let { { Text(it.asString()) } },
                isError = state.confirmPasswordError != null,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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

            Spacer(Modifier.height(8.dp))

            // Create account button
            HLButton(
                onClick = { onAction(EmailRegisterAction.OnSubmit) },
                enabled = state.isSubmitEnabled,
                leadingIcon = if (state.isLoading) {
                    {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = HLColors.Primary
                        )
                    }
                } else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(Res.string.create_account_button),
                    style = HLTypography.labelLarge
                )
            }

            // Login link
            TextButton(
                onClick = { onAction(EmailRegisterAction.OnNavigateToLogin) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = stringResource(Res.string.have_account),
                    style = HLTypography.labelLarge,
                    color = HLColors.Primary
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

/**
 * Preview parameter provider for EmailRegisterScreen.
 * Provides different states: default, filled, with errors, loading, password mismatch, weak password.
 */
private class EmailRegisterStatePreviewProvider : PreviewParameterProvider<EmailRegisterState> {
    override val values: Sequence<EmailRegisterState> = sequenceOf(
        // Default - empty fields
        EmailRegisterState(
            email = "",
            password = "",
            confirmPassword = "",
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            isLoading = false
        ),
        // Filled fields - valid input, submit enabled
        EmailRegisterState(
            email = "user@example.com",
            password = "password123",
            confirmPassword = "password123",
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            isLoading = false
        ),
        // With all validation errors
        EmailRegisterState(
            email = "invalid",
            password = "123",
            confirmPassword = "456",
            emailError = UiText.DynamicString("Invalid email"),
            passwordError = UiText.DynamicString("Password too short"),
            confirmPasswordError = UiText.DynamicString("Passwords don't match"),
            isLoading = false
        ),
        // Loading state
        EmailRegisterState(
            email = "user@example.com",
            password = "password123",
            confirmPassword = "password123",
            emailError = null,
            passwordError = null,
            confirmPasswordError = null,
            isLoading = true
        ),
        // Password mismatch error only
        EmailRegisterState(
            email = "user@example.com",
            password = "password123",
            confirmPassword = "password456",
            emailError = null,
            passwordError = null,
            confirmPasswordError = UiText.DynamicString("Passwords don't match"),
            isLoading = false
        ),
        // Weak password error only
        EmailRegisterState(
            email = "user@example.com",
            password = "123",
            confirmPassword = "123",
            emailError = null,
            passwordError = UiText.DynamicString("Password must be at least 6 characters"),
            confirmPasswordError = null,
            isLoading = false
        )
    )
}

@Preview
@Composable
private fun EmailRegisterScreenPreview(
    @PreviewParameter(EmailRegisterStatePreviewProvider::class) state: EmailRegisterState
) {
    HLTheme {
        EmailRegisterScreen(
            state = state,
            onAction = {},
            onNavigateBack = {}
        )
    }
}
