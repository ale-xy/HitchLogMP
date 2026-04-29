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
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircle
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
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.ColorRole
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.chipColorsForRole

/**
 * Size variants for icon badges.
 */
enum class IconBadgeSize(val iconSize: Dp, val badgeSize: Dp) {
    SMALL(18.dp, 28.dp),
    MEDIUM(18.dp, 32.dp),
    LARGE(22.dp, 40.dp)
}

/**
 * Circular icon badge with semantic color role.
 * Used throughout the app for record types, actions, and status indicators.
 *
 * @param icon The icon to display
 * @param colorRole Semantic color role that determines background/foreground colors
 * @param size Size variant (SMALL, MEDIUM, LARGE)
 * @param modifier Optional modifier
 */
@Composable
fun HLIconBadge(
    icon: ImageVector,
    colorRole: ColorRole,
    size: IconBadgeSize = IconBadgeSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    val chipColors = chipColorsForRole(colorRole)
    
    HLIconBadge(
        icon = icon,
        chipColors = chipColors,
        size = size,
        modifier = modifier
    )
}

/**
 * Circular icon badge with explicit chip colors.
 * Use this variant when you need custom colors not covered by ColorRole.
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
                colorRole = ColorRole.PRIMARY,
                size = IconBadgeSize.SMALL
            )
            HLIconBadge(
                icon = Icons.Filled.DirectionsCar,
                colorRole = ColorRole.PRIMARY,
                size = IconBadgeSize.MEDIUM
            )
            HLIconBadge(
                icon = Icons.Filled.DirectionsCar,
                colorRole = ColorRole.PRIMARY,
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
                    icon = Icons.Filled.Flag,
                    colorRole = ColorRole.PRIMARY,
                    size = IconBadgeSize.MEDIUM
                )
                Text("PRIMARY", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.Filled.DirectionsCar,
                    colorRole = ColorRole.SECONDARY,
                    size = IconBadgeSize.MEDIUM
                )
                Text("SECONDARY", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    colorRole = ColorRole.TERTIARY,
                    size = IconBadgeSize.MEDIUM
                )
                Text("TERTIARY", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.Filled.PauseCircle,
                    colorRole = ColorRole.ERROR,
                    size = IconBadgeSize.MEDIUM
                )
                Text("ERROR", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.Filled.Block,
                    colorRole = ColorRole.ERROR_BOLD,
                    size = IconBadgeSize.MEDIUM
                )
                Text("ERROR_BOLD", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.Filled.Group,
                    colorRole = ColorRole.OUTLINE,
                    size = IconBadgeSize.MEDIUM
                )
                Text("OUTLINE", style = HLTypography.bodyMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HLIconBadge(
                    icon = Icons.Filled.Hotel,
                    colorRole = ColorRole.SURFACE,
                    size = IconBadgeSize.MEDIUM
                )
                Text("SURFACE", style = HLTypography.bodyMedium)
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
                    colorRole = ColorRole.PRIMARY,
                    size = IconBadgeSize.LARGE
                )
                HLIconBadge(
                    icon = Icons.Filled.DirectionsCar,
                    colorRole = ColorRole.SECONDARY,
                    size = IconBadgeSize.LARGE
                )
                HLIconBadge(
                    icon = Icons.Filled.LocationOn,
                    colorRole = ColorRole.PRIMARY,
                    size = IconBadgeSize.LARGE
                )
                HLIconBadge(
                    icon = Icons.Filled.Hotel,
                    colorRole = ColorRole.SURFACE,
                    size = IconBadgeSize.LARGE
                )
                HLIconBadge(
                    icon = Icons.Filled.Flag,
                    colorRole = ColorRole.PRIMARY,
                    size = IconBadgeSize.LARGE
                )
            }
        }
    }
}
