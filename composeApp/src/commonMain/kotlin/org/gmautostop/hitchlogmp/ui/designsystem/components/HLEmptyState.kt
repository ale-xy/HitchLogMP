package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Empty state component for screens with no content.
 * Displays an icon, message, and up to two action buttons.
 *
 * @param icon Icon representing the empty state
 * @param message Message explaining the empty state
 * @param primaryAction Optional primary action (label + handler)
 * @param secondaryAction Optional secondary action (label + handler)
 * @param modifier Optional modifier
 */
@Composable
fun HLEmptyState(
    icon: ImageVector,
    message: String,
    primaryAction: Pair<String, () -> Unit>? = null,
    secondaryAction: Pair<String, () -> Unit>? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = HLSpacing.xxl, vertical = HLSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = HLColors.OutlineVariant,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(Modifier.height(HLSpacing.xl))
        
        Text(
            text = message,
            style = HLTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            color = HLColors.OnSurface
        )
        
        if (primaryAction != null) {
            Spacer(Modifier.height(28.dp))
            
            Row(
                Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(HLColors.Primary)
                    .clickable(onClick = primaryAction.second)
                    .padding(start = 22.dp, end = 28.dp, top = HLSpacing.xl, bottom = HLSpacing.xl),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(HLSpacing.lg)
            ) {
                Text(
                    text = primaryAction.first,
                    style = HLTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = HLColors.OnPrimary
                )
            }
        }
        
        if (secondaryAction != null) {
            Spacer(Modifier.height(HLSpacing.lg))
            
            Box(
                Modifier
                    .clip(HLShapes.extraLarge)
                    .clickable(onClick = secondaryAction.second)
                    .padding(horizontal = HLSpacing.xl, vertical = 10.dp)
            ) {
                Text(
                    text = secondaryAction.first,
                    style = HLTypography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = HLColors.Primary
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun EmptyStateBasicPreview() {
    HLTheme {
        Box(Modifier.fillMaxSize().background(HLColors.Background)) {
            HLEmptyState(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                message = "Хроника пуста. Начните гонку!"
            )
        }
    }
}

@Preview
@Composable
private fun EmptyStateWithActionsPreview() {
    HLTheme {
        Box(Modifier.fillMaxSize().background(HLColors.Background)) {
            HLEmptyState(
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                message = "Хроника пуста. Начните гонку!",
                primaryAction = "Старт" to { },
                secondaryAction = "Другой тип записи" to { }
            )
        }
    }
}
