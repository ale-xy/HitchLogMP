package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Button style variants following Material 3 design.
 */
enum class ButtonVariant {
    /** Filled button with primary background */
    Filled,
    /** Tonal button with primary container background */
    Tonal,
    /** Outlined button with transparent background and border */
    Outlined,
    /** Text button with transparent background */
    Text
}

/**
 * General-purpose button component for HitchLog application.
 * Follows Material 3 design with 48dp height and fully rounded corners.
 *
 * @param onClick Click handler
 * @param modifier Optional modifier
 * @param variant Button style variant (Filled, Tonal, Outlined, Text)
 * @param enabled Whether button is enabled
 * @param leadingIcon Optional leading icon composable
 * @param content Button content (typically Text)
 */
@Composable
fun HLButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Filled,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val colors = getButtonColors(variant, isPressed, enabled)
    val shape = RoundedCornerShape(24.dp)
    
    CompositionLocalProvider(LocalContentColor provides colors.foreground) {
        Row(
            modifier = modifier
                .height(48.dp)
                .clip(shape)
                .then(
                    if (variant == ButtonVariant.Outlined) {
                        Modifier.border(
                            1.dp, 
                            if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant, 
                            shape
                        )
                    } else Modifier
                )
                .background(colors.background)
                .then(
                    if (!enabled) Modifier.alpha(0.7f) else Modifier
                )
                .clickable(
                    onClick = onClick,
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null // Use custom pressed state instead of ripple
                )
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
            }
            content()
        }
    }
}

/**
 * Button color configuration for background and foreground.
 */
private data class ButtonColors(
    val background: Color,
    val foreground: Color
)

/**
 * Returns button colors based on variant, pressed state, and enabled state.
 */
@Composable
private fun getButtonColors(variant: ButtonVariant, isPressed: Boolean, enabled: Boolean): ButtonColors {
    if (!enabled) {
        return ButtonColors(
            background = MaterialTheme.colorScheme.surfaceVariant,
            foreground = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    return when (variant) {
        ButtonVariant.Filled -> ButtonColors(
            background = if (isPressed) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
            foreground = if (isPressed) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary
        )
        ButtonVariant.Tonal -> ButtonColors(
            background = if (isPressed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            foreground = if (isPressed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
        )
        ButtonVariant.Outlined -> ButtonColors(
            background = if (isPressed) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent,
            foreground = MaterialTheme.colorScheme.onSurface
        )
        ButtonVariant.Text -> ButtonColors(
            background = if (isPressed) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent,
            foreground = MaterialTheme.colorScheme.primary
        )
    }
}
