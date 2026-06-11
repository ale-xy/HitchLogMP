package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.ChipColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.RecordColor
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.recordChipColors

/**
 * Size variants for icon badges.
 */
enum class IconBadgeSize(val iconSize: Dp, val badgeSize: Dp) {
    SMALL(18.dp, 28.dp),
    MEDIUM(18.dp, 32.dp),
    LARGE(22.dp, 40.dp)
}

/**
 * Circular icon badge with explicit chip colors.
 *
 * @param icon The icon to display
 * @param chipColors Explicit background, foreground, and optional stroke colors
 * @param size Size variant (SMALL, MEDIUM, LARGE)
 * @param modifier Optional modifier
 */
@Composable
fun HLIconBadge(
    icon: ImageVector,
    chipColors: ChipColors,
    size: IconBadgeSize = IconBadgeSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.badgeSize)
            .clip(CircleShape)
            .background(chipColors.bg)
            .then(
                if (chipColors.stroke != null) {
                    Modifier.border(1.dp, chipColors.stroke, CircleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = chipColors.fg,
            modifier = Modifier.size(size.iconSize)
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun IconBadgeSizesPreview() {
    HLTheme {
        Row(
            Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HLIconBadge(
                icon = Icons.Filled.DirectionsCar,
                chipColors = recordChipColors(RecordColor.YELLOW, end = false),
                size = IconBadgeSize.SMALL
            )
            HLIconBadge(
                icon = Icons.Filled.DirectionsCar,
                chipColors = recordChipColors(RecordColor.YELLOW, end = false),
                size = IconBadgeSize.MEDIUM
            )
            HLIconBadge(
                icon = Icons.Filled.DirectionsCar,
                chipColors = recordChipColors(RecordColor.YELLOW, end = false),
                size = IconBadgeSize.LARGE
            )
        }
    }
}

@Preview
@Composable
private fun IconBadgeColorsPreview() {
    HLTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.Filled.SportsScore,
                    chipColors = recordChipColors(RecordColor.BLUE, end = false),
                    size = IconBadgeSize.MEDIUM
                )
                Text("BLUE", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    chipColors = recordChipColors(RecordColor.ORANGE, end = false),
                    size = IconBadgeSize.MEDIUM
                )
                Text("ORANGE", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.Filled.PauseCircle,
                    chipColors = recordChipColors(RecordColor.MAGENTA, end = false),
                    size = IconBadgeSize.MEDIUM
                )
                Text("MAGENTA", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.Filled.Hotel,
                    chipColors = recordChipColors(RecordColor.GREEN, end = false),
                    size = IconBadgeSize.MEDIUM
                )
                Text("GREEN", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.Filled.Group,
                    chipColors = recordChipColors(RecordColor.NEUTRAL_DARK, end = false),
                    size = IconBadgeSize.MEDIUM
                )
                Text("NEUTRAL_DARK", style = HLTypography.bodyMedium)
            }
            // Outlined contour ("end") chip variant
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    chipColors = recordChipColors(RecordColor.YELLOW, end = true),
                    size = IconBadgeSize.MEDIUM
                )
                Text("YELLOW (end)", style = HLTypography.bodyMedium)
            }
        }
    }
}

@Preview
@Composable
private fun IconBadgeRecordTypesPreview() {
    HLTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HLIconBadge(
                    icon = Icons.Filled.Timer,
                    chipColors = recordChipColors(RecordColor.BLUE, end = false),
                    size = IconBadgeSize.LARGE
                )
                HLIconBadge(
                    icon = Icons.Filled.DirectionsCar,
                    chipColors = recordChipColors(RecordColor.YELLOW, end = false),
                    size = IconBadgeSize.LARGE
                )
                HLIconBadge(
                    icon = Icons.Filled.LocationOn,
                    chipColors = recordChipColors(RecordColor.BLUE, end = false),
                    size = IconBadgeSize.LARGE
                )
                HLIconBadge(
                    icon = Icons.Filled.LightMode,
                    chipColors = recordChipColors(RecordColor.GREEN, end = true),
                    size = IconBadgeSize.LARGE
                )
                HLIconBadge(
                    icon = Icons.Filled.SportsScore,
                    chipColors = recordChipColors(RecordColor.BLUE, end = false),
                    size = IconBadgeSize.LARGE
                )
            }
        }
    }
}
