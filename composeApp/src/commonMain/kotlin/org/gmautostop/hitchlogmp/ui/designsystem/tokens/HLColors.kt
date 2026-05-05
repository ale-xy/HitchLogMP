package org.gmautostop.hitchlogmp.ui.designsystem.tokens

import androidx.compose.ui.graphics.Color

/**
 * HitchLog color palette following Material3 design principles.
 * All colors are defined as semantic tokens for consistent theming.
 */
object HLColors {
    // Primary colors - used for key actions and important UI elements
    val Primary = Color(0xFF1A3A8F)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFFD9E2FF)
    val OnPrimaryContainer = Color(0xFF001258)

    // Secondary colors - used for less prominent actions and accents
    val Secondary = Color(0xFFFFCC00)
    val OnSecondary = Color(0xFF1A1A00)

    // Tertiary colors - used for contrasting accents
    val Tertiary = Color(0xFF5C6BC0)
    val OnTertiary = Color.White

    // Error colors - used for errors and destructive actions
    val Error = Color(0xFFBA1A1A)
    val OnError = Color.White
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    // Surface colors - used for backgrounds and containers
    val Background = Color(0xFFF8F9FF)
    val Surface = Color.White
    val OnSurface = Color(0xFF1A1B20)
    val SurfaceVariant = Color(0xFFE4E5F0)
    val OnSurfaceVariant = Color(0xFF44464F)
    val SurfaceContainerLow = Color(0xFFF2F3FA)
    val SurfaceContainer = Color(0xFFECEDF4)

    // Outline colors - used for borders and dividers
    val Outline = Color(0xFF74757F)
    val OutlineVariant = Color(0xFFC4C5D0)
}

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
 */
fun chipColorsForRole(role: ColorRole): ChipColors = when (role) {
    ColorRole.PRIMARY -> ChipColors(HLColors.Primary, HLColors.OnPrimary)
    ColorRole.SECONDARY -> ChipColors(HLColors.Secondary, HLColors.OnSecondary)
    ColorRole.TERTIARY -> ChipColors(HLColors.Tertiary, HLColors.OnTertiary)
    ColorRole.ERROR -> ChipColors(HLColors.ErrorContainer, HLColors.OnErrorContainer, HLColors.Error)
    ColorRole.ERROR_BOLD -> ChipColors(HLColors.Error, HLColors.OnError)
    ColorRole.OUTLINE -> ChipColors(HLColors.Surface, HLColors.OnSurfaceVariant, HLColors.OutlineVariant)
    ColorRole.SURFACE -> ChipColors(HLColors.SurfaceVariant, HLColors.OnSurfaceVariant)
}
