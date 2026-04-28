package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
 * Status badge component for displaying live race status.
 * Pill-shaped badge with icon, label, and optional subtitle.
 *
 * @param icon Icon representing the status
 * @param label Main status text (e.g., "В машине", "Отдых")
 * @param backgroundColor Badge background color
 * @param foregroundColor Text and icon color
 * @param subtitle Optional subtitle text (e.g., "· с 14:30")
 * @param modifier Optional modifier
 */
@Composable
fun HLStatusBadge(
    icon: ImageVector,
    label: String,
    backgroundColor: Color,
    foregroundColor: Color,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(HLShapes.pill)
            .background(backgroundColor)
            .padding(vertical = HLSpacing.sm, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HLSpacing.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = foregroundColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = HLTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = foregroundColor
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = HLTypography.labelMedium,
                color = foregroundColor.copy(alpha = 0.75f)
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun StatusBadgeVariantsPreview() {
    HLTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HLStatusBadge(
                icon = Icons.Filled.DirectionsCar,
                label = "В машине",
                backgroundColor = HLColors.Secondary,
                foregroundColor = HLColors.OnSecondary
            )
            HLStatusBadge(
                icon = Icons.Filled.Hotel,
                label = "Отдых",
                backgroundColor = HLColors.SurfaceVariant,
                foregroundColor = HLColors.OnSurfaceVariant
            )
            HLStatusBadge(
                icon = Icons.Filled.PauseCircle,
                label = "Вне игры",
                backgroundColor = HLColors.ErrorContainer,
                foregroundColor = HLColors.OnErrorContainer
            )
            HLStatusBadge(
                icon = Icons.Filled.Flag,
                label = "Финиш",
                backgroundColor = HLColors.Primary,
                foregroundColor = HLColors.OnPrimary
            )
            HLStatusBadge(
                icon = Icons.Filled.Cancel,
                label = "Сход",
                backgroundColor = HLColors.Error,
                foregroundColor = HLColors.OnError
            )
        }
    }
}

@Preview
@Composable
private fun StatusBadgeWithSubtitlePreview() {
    HLTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HLStatusBadge(
                icon = Icons.Filled.DirectionsCar,
                label = "В машине",
                backgroundColor = HLColors.Secondary,
                foregroundColor = HLColors.OnSecondary,
                subtitle = "· с 14:30"
            )
            HLStatusBadge(
                icon = Icons.Filled.Hotel,
                label = "Отдых",
                backgroundColor = HLColors.SurfaceVariant,
                foregroundColor = HLColors.OnSurfaceVariant,
                subtitle = "· с 18:45"
            )
        }
    }
}
