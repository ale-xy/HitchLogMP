package org.gmautostop.hitchlogmp.ui.designsystem.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Semantic color roles for record type badges and icons.
 * Maps domain concepts to visual styling.
 */
enum class ColorRole {
    PRIMARY,      // Start, Checkpoint, Finish
    SECONDARY,    // Lift, Get Off
    TERTIARY,     // Walk, Walk End
    ERROR,        // Offside (with stroke)
    ERROR_BOLD,   // Retire (solid error)
    OUTLINE,      // Meet (neutral with border)
    SURFACE       // Rest, Free Text (subtle)
}

/**
 * Color configuration for icon badges and chips.
 * Includes background, foreground, and optional stroke colors.
 */
data class ChipColors(
    val bg: Color,
    val fg: Color,
    val stroke: Color? = null
)

/**
 * Maps a ColorRole to its corresponding ChipColors configuration.
 * Reads colors from MaterialTheme.colorScheme to support light and dark themes.
 */
@Composable
fun chipColorsForRole(role: ColorRole): ChipColors {
    val colorScheme = MaterialTheme.colorScheme
    return when (role) {
        ColorRole.PRIMARY -> ChipColors(colorScheme.primary, colorScheme.onPrimary)
        ColorRole.SECONDARY -> ChipColors(colorScheme.secondary, colorScheme.onSecondary)
        ColorRole.TERTIARY -> ChipColors(colorScheme.tertiary, colorScheme.onTertiary)
        ColorRole.ERROR -> ChipColors(colorScheme.errorContainer, colorScheme.onErrorContainer, colorScheme.error)
        ColorRole.ERROR_BOLD -> ChipColors(colorScheme.error, colorScheme.onError)
        ColorRole.OUTLINE -> ChipColors(colorScheme.surface, colorScheme.onSurfaceVariant, colorScheme.outlineVariant)
        ColorRole.SURFACE -> ChipColors(colorScheme.surfaceVariant, colorScheme.onSurfaceVariant)
    }
}
