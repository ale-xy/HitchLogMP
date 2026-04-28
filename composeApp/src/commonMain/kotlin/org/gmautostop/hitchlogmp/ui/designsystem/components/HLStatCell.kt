package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Stat cell component for displaying metrics with icon, value, and optional label.
 * Used in summary cards to show lifts, checkpoints, rest time, etc.
 *
 * @param icon Icon representing the metric
 * @param value The metric value (e.g., "5", "2:30")
 * @param label Optional label below the value (e.g., "использовано", "осталось")
 * @param onClick Optional click handler (makes the cell interactive)
 * @param align Horizontal alignment of content (Start or End)
 * @param modifier Optional modifier
 */
@Composable
fun HLStatCell(
    icon: ImageVector,
    value: String,
    label: String? = null,
    onClick: (() -> Unit)? = null,
    align: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier
) {
    val cellModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .then(Modifier.padding(HLSpacing.sm))
    } else {
        modifier
    }

    Column(
        cellModifier,
        horizontalAlignment = align
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(HLSpacing.sm)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = HLColors.OnPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = value,
                style = HLTypography.statValue,
                color = HLColors.OnPrimaryContainer
            )
        }
        if (label != null) {
            Text(
                text = label,
                style = HLTypography.labelSmall,
                color = HLColors.OnPrimaryContainer.copy(alpha = 0.75f)
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun StatCellBasicPreview() {
    HLTheme {
        Box(
            Modifier
                .background(HLColors.PrimaryContainer)
                .padding(16.dp)
        ) {
            HLStatCell(
                icon = Icons.Filled.DirectionsCar,
                value = "5"
            )
        }
    }
}

@Preview
@Composable
private fun StatCellWithLabelPreview() {
    HLTheme {
        Box(
            Modifier
                .background(HLColors.PrimaryContainer)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                HLStatCell(
                    icon = Icons.Filled.Hotel,
                    value = "02:30",
                    label = "использовано"
                )
                HLStatCell(
                    icon = Icons.Filled.LocationOn,
                    value = "3",
                    label = "КП"
                )
            }
        }
    }
}

@Preview
@Composable
private fun StatCellClickablePreview() {
    HLTheme {
        Box(
            Modifier
                .background(HLColors.PrimaryContainer)
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                HLStatCell(
                    icon = Icons.Filled.Hotel,
                    value = "02:30",
                    label = "использовано",
                    onClick = { },
                    align = Alignment.Start
                )
                HLStatCell(
                    icon = Icons.Filled.Hotel,
                    value = "01:30",
                    label = "осталось",
                    onClick = { },
                    align = Alignment.End
                )
            }
        }
    }
}
