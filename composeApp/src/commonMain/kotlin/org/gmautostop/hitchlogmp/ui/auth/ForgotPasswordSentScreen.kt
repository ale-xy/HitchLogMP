package org.gmautostop.hitchlogmp.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.back_to_login
import hitchlogmp.composeapp.generated.resources.email_sent_description
import hitchlogmp.composeapp.generated.resources.email_sent_icon_desc
import hitchlogmp.composeapp.generated.resources.email_sent_title
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.jetbrains.compose.resources.stringResource

/**
 * Stateless screen composable - pure state.
 */
@Composable
fun ForgotPasswordSentScreen(
    email: String,
    onNavigateToAuth: () -> Unit
) {
    Scaffold(
        containerColor = HLColors.Background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon container
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = HLColors.PrimaryContainer,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MarkEmailRead,
                        contentDescription = stringResource(Res.string.email_sent_icon_desc),
                        tint = HLColors.OnPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Title
                Text(
                    text = stringResource(Res.string.email_sent_title),
                    style = HLTypography.titleLarge,
                    color = HLColors.OnSurface,
                    textAlign = TextAlign.Center
                )

                // Description with email
                Text(
                    text = stringResource(Res.string.email_sent_description, email),
                    style = HLTypography.bodyMedium,
                    color = HLColors.OnSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(max = 280.dp)
                )

                Spacer(Modifier.height(8.dp))

                // Back to login button
                TextButton(onClick = onNavigateToAuth) {
                    Text(
                        text = stringResource(Res.string.back_to_login),
                        style = HLTypography.labelLarge,
                        color = HLColors.Primary
                    )
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ForgotPasswordSentScreenPreview() {
    HLTheme {
        ForgotPasswordSentScreen(
            email = "user@example.com",
            onNavigateToAuth = {}
        )
    }
}

@Preview
@Composable
private fun ForgotPasswordSentScreenLongEmailPreview() {
    HLTheme {
        ForgotPasswordSentScreen(
            email = "very.long.email.address@example.com",
            onNavigateToAuth = {}
        )
    }
}
