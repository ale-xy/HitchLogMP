package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes

/**
 * Size variants for stepper buttons.
 */
enum class StepperButtonSize {
    /** Small size (36dp) - deprecated, use MEDIUM */
    SMALL,
    /** Medium size (40dp) - standard size for all steppers */
    MEDIUM
}

/**
 * Circular +/- button for date/time adjustment.
 * Used in EditRecordScreen for incrementing/decrementing values.
 *
 * @param icon The icon to display (Add or Remove)
 * @param onClick Click handler
 * @param size Size variant (SMALL or MEDIUM)
 * @param contentDescription Accessibility description
 * @param modifier Optional modifier
 */
@Composable
fun HLStepperButton(
    icon: ImageVector,
    onClick: () -> Unit,
    size: StepperButtonSize = StepperButtonSize.MEDIUM,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val buttonSize = when (size) {
        StepperButtonSize.SMALL -> 36.dp
        StepperButtonSize.MEDIUM -> 40.dp
    }
    
    val iconSize = when (size) {
        StepperButtonSize.SMALL -> 20.dp
        StepperButtonSize.MEDIUM -> 24.dp
    }

    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(HLShapes.pill)
            .background(HLColors.SurfaceContainerLow)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = HLColors.Primary,
            modifier = Modifier.size(iconSize)
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun StepperButtonPreview() {
    HLTheme {
        Column(
            Modifier
                .background(HLColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HLStepperButton(
                    icon = Icons.Default.Remove,
                    onClick = { },
                    size = StepperButtonSize.SMALL,
                    contentDescription = "Subtract"
                )
                HLStepperButton(
                    icon = Icons.Default.Add,
                    onClick = { },
                    size = StepperButtonSize.SMALL,
                    contentDescription = "Add"
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HLStepperButton(
                    icon = Icons.Default.Remove,
                    onClick = { },
                    size = StepperButtonSize.MEDIUM,
                    contentDescription = "Subtract"
                )
                HLStepperButton(
                    icon = Icons.Default.Add,
                    onClick = { },
                    size = StepperButtonSize.MEDIUM,
                    contentDescription = "Add"
                )
            }
        }
    }
}
