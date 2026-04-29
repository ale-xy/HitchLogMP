package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Time adjustment shortcut chip.
 * Used in EditRecordScreen for quick time adjustments (-10, -5, 0, +5, +10).
 *
 * @param label The label to display (e.g., "-10", "0", "+5")
 * @param onClick Click handler
 * @param isPrimary Whether this is the primary action (0 button)
 * @param modifier Optional modifier
 */
@Composable
fun HLShortcutChip(
    label: String,
    onClick: () -> Unit,
    isPrimary: Boolean = false,
    modifier: Modifier = Modifier
) {
    val background = if (isPrimary) HLColors.Primary else HLColors.SurfaceContainerLow
    val foreground = if (isPrimary) HLColors.OnPrimary else HLColors.Primary
    val fontSize = if (isPrimary) 15.sp else 13.sp

    Box(
        modifier = modifier
            .height(34.dp)
            .clip(HLShapes.pill)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = HLTypography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = fontSize
            ),
            color = foreground
        )
    }
}

/**
 * Rail of 5 time shortcut chips with equal weight.
 * Displays -10, -5, 0, +5, +10 minute adjustments.
 *
 * @param onShortcut Callback with the minutes to adjust (0 means "now")
 * @param modifier Optional modifier
 */
@Composable
fun HLShortcutChipRail(
    onShortcut: (minutes: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HLShortcutChip(
            label = "-10",
            onClick = { onShortcut(-10) },
            modifier = Modifier.weight(1f)
        )
        HLShortcutChip(
            label = "-5",
            onClick = { onShortcut(-5) },
            modifier = Modifier.weight(1f)
        )
        HLShortcutChip(
            label = "0",
            onClick = { onShortcut(0) },
            isPrimary = true,
            modifier = Modifier.weight(1f)
        )
        HLShortcutChip(
            label = "+5",
            onClick = { onShortcut(5) },
            modifier = Modifier.weight(1f)
        )
        HLShortcutChip(
            label = "+10",
            onClick = { onShortcut(10) },
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ShortcutChipPreview() {
    HLTheme {
        Column(
            Modifier
                .background(HLColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HLShortcutChip(
                    label = "-10",
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
                HLShortcutChip(
                    label = "0",
                    onClick = { },
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
                HLShortcutChip(
                    label = "+10",
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview
@Composable
private fun ShortcutChipRailPreview() {
    HLTheme {
        Box(
            Modifier
                .background(HLColors.Background)
                .padding(16.dp)
        ) {
            HLShortcutChipRail(onShortcut = { })
        }
    }
}
