package org.gmautostop.hitchlogmp.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.anonymous_login_confirm
import hitchlogmp.composeapp.generated.resources.anonymous_login_warning_message
import hitchlogmp.composeapp.generated.resources.anonymous_login_warning_title
import hitchlogmp.composeapp.generated.resources.auth_anonymous_icon_desc
import hitchlogmp.composeapp.generated.resources.auth_anonymous_login
import hitchlogmp.composeapp.generated.resources.auth_apple_icon_desc
import hitchlogmp.composeapp.generated.resources.auth_apple_login
import hitchlogmp.composeapp.generated.resources.auth_divider_or
import hitchlogmp.composeapp.generated.resources.auth_email_icon_desc
import hitchlogmp.composeapp.generated.resources.auth_email_login
import hitchlogmp.composeapp.generated.resources.auth_google_icon_desc
import hitchlogmp.composeapp.generated.resources.auth_google_login
import hitchlogmp.composeapp.generated.resources.auth_logo_desc
import hitchlogmp.composeapp.generated.resources.auth_subtitle
import hitchlogmp.composeapp.generated.resources.auth_title
import hitchlogmp.composeapp.generated.resources.cancel
import hitchlogmp.composeapp.generated.resources.gma_logo
import hitchlogmp.composeapp.generated.resources.ic_apple
import hitchlogmp.composeapp.generated.resources.ic_google
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.ui.ObserveAsEvents
import org.gmautostop.hitchlogmp.ui.asStringSuspend
import org.gmautostop.hitchlogmp.ui.designsystem.components.ButtonVariant
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLButton
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLConfirmationDialog
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable - holds ViewModel and observes events.
 */
@Composable
fun AuthScreen(
    onAuthenticated: () -> Unit,
    onNavigateToEmailLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is AuthEvent.NavigateToLogList -> onAuthenticated()
            is AuthEvent.NavigateToEmailLogin -> onNavigateToEmailLogin()
            is AuthEvent.ShowError -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.error.asStringSuspend())
                }
            }
        }
    }

    AuthScreen(
        state = state,
        onAction = viewModel::onAction,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Stateless screen composable - pure state + onAction.
 */
@Composable
fun AuthScreen(
    state: AuthState,
    onAction: (AuthAction) -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        containerColor = HLColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top section: Logo + Title + Subtitle
                Spacer(Modifier.height(80.dp))
                
                Image(
                    painter = painterResource(Res.drawable.gma_logo),
                    contentDescription = stringResource(Res.string.auth_logo_desc),
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = stringResource(Res.string.auth_title),
                    style = HLTypography.displaySmall,
                    color = HLColors.OnSurface
                )
                
                Spacer(Modifier.height(6.dp))
                
                Text(
                    text = stringResource(Res.string.auth_subtitle),
                    style = HLTypography.bodyMedium,
                    color = HLColors.OnSurfaceVariant
                )
                
                Spacer(Modifier.weight(1f))
                
                // Bottom section: Auth buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Email button (tonal, placeholder)
                    HLButton(
                        onClick = { onAction(AuthAction.OnEmailLoginClick) },
                        variant = ButtonVariant.Tonal,
                        enabled = !state.isGoogleLoading && !state.isAppleLoading && !state.isAnonymousLoading,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = stringResource(Res.string.auth_email_icon_desc),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(Res.string.auth_email_login),
                            style = HLTypography.bodyLarge
                        )
                    }
                    
                    // Google button (outlined, use KMPAuth)
                    GoogleButtonUiContainerFirebase(
                        modifier = Modifier.fillMaxWidth(),
                        linkAccount = true,
                        onResult = { result ->
                            val firebaseUser = if (result.isSuccess) result.getOrNull() else null
                            onAction(AuthAction.OnGoogleLoginResult(firebaseUser))
                            
                            // Handle error case
                            if (result.isFailure) {
                                val error = result.exceptionOrNull()
                                onAction(AuthAction.OnGoogleLoginResult(null))
                            }
                        }
                    ) {
                        HLButton(
                            onClick = {
                                onAction(AuthAction.OnGoogleLoginClick)
                                this.onClick()
                            },
                            variant = ButtonVariant.Outlined,
                            enabled = !state.isGoogleLoading && !state.isAppleLoading && !state.isAnonymousLoading,
                            leadingIcon = {
                    if (state.isGoogleLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = HLColors.Primary
                        )
                                } else {
                                    Image(
                                        painter = painterResource(Res.drawable.ic_google),
                                        contentDescription = stringResource(Res.string.auth_google_icon_desc),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(Res.string.auth_google_login),
                                style = HLTypography.bodyLarge
                            )
                        }
                    }
                    
                    // Apple button (outlined, placeholder)
                    HLButton(
                        onClick = { onAction(AuthAction.OnAppleLoginClick) },
                        variant = ButtonVariant.Outlined,
                        enabled = !state.isGoogleLoading && !state.isAppleLoading && !state.isAnonymousLoading,
                        leadingIcon = {
                            Image(
                                painter = painterResource(Res.drawable.ic_apple),
                                contentDescription = stringResource(Res.string.auth_apple_icon_desc),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(Res.string.auth_apple_login),
                            style = HLTypography.bodyLarge
                        )
                    }
                    
                    // Divider with "или"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = HLColors.OutlineVariant
                        )
                        Text(
                            text = stringResource(Res.string.auth_divider_or),
                            style = HLTypography.bodySmall,
                            color = HLColors.OnSurfaceVariant
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            thickness = 1.dp,
                            color = HLColors.OutlineVariant
                        )
                    }
                    
                    // Anonymous button (text variant)
                    HLButton(
                        onClick = { onAction(AuthAction.OnAnonymousLoginClick) },
                        variant = ButtonVariant.Text,
                        enabled = !state.isGoogleLoading && !state.isAppleLoading && !state.isAnonymousLoading,
                        leadingIcon = {
                    if (state.isAnonymousLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.5.dp,
                            color = HLColors.Primary
                        )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PersonOff,
                                    contentDescription = stringResource(Res.string.auth_anonymous_icon_desc),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(Res.string.auth_anonymous_login),
                            style = HLTypography.bodyLarge
                        )
                    }
                }
            }
            
            // Anonymous Login Warning Dialog
            HLConfirmationDialog(
                visible = state.showAnonymousWarningDialog,
                onDismiss = { onAction(AuthAction.OnDismissAnonymousWarningDialog) },
                title = stringResource(Res.string.anonymous_login_warning_title),
                message = stringResource(Res.string.anonymous_login_warning_message),
                confirmLabel = stringResource(Res.string.anonymous_login_confirm),
                cancelLabel = stringResource(Res.string.cancel),
                onConfirm = { onAction(AuthAction.OnConfirmAnonymousLogin) },
                icon = Icons.Default.PersonOff,
                isDestructive = true
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

/**
 * Preview parameter provider for AuthState.
 * Provides different states for preview: default, loading Google, loading Anonymous.
 */
private class AuthStatePreviewProvider : PreviewParameterProvider<AuthState> {
    override val values: Sequence<AuthState> = sequenceOf(
        AuthState(),
        AuthState(isGoogleLoading = true),
        AuthState(isAnonymousLoading = true)
    )
}

@Preview
@Composable
private fun AuthScreenPreview(
    @PreviewParameter(AuthStatePreviewProvider::class) state: AuthState
) {
    HLTheme {
        AuthScreen(
            state = state,
            onAction = {}
        )
    }
}
