package org.gmautostop.hitchlogmp.ui.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended colors beyond Material 3 color scheme.
 * Used for custom UI elements like the rest hint banner.
 */
data class HLExtendedColors(
    val restBannerBg: Color,
    val restBannerText: Color,
    val restBannerBorder: Color
)

/**
 * CompositionLocal for accessing extended colors in composables.
 */
val LocalHLExtendedColors = staticCompositionLocalOf {
    HLExtendedColors(
        restBannerBg = Color(0xFFFFF8E1),
        restBannerText = Color(0xFF854F0B),
        restBannerBorder = Color(0xFFFFE082)
    )
}

/**
 * Light theme extended colors.
 */
private val LightExtended = HLExtendedColors(
    restBannerBg = Color(0xFFFFF8E1),
    restBannerText = Color(0xFF854F0B),
    restBannerBorder = Color(0xFFFFE082)
)

/**
 * Dark theme extended colors.
 */
private val DarkExtended = HLExtendedColors(
    restBannerBg = Color(0xFF2A2410),
    restBannerText = Color(0xFFFFE082),
    restBannerBorder = Color(0xFF4A3D1A)
)

/**
 * Light color scheme following Material 3 design.
 */
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1A3A8F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E2FF),
    onPrimaryContainer = Color(0xFF001258),
    secondary = Color(0xFFFFCC00),
    onSecondary = Color(0xFF1A1A00),
    secondaryContainer = Color(0xFFFFF0A0),
    onSecondaryContainer = Color(0xFF1A1400),
    tertiary = Color(0xFF5C6BC0),
    onTertiary = Color.White,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF8F9FF),
    surface = Color.White,
    onSurface = Color(0xFF1A1B20),
    surfaceVariant = Color(0xFFE4E5F0),
    onSurfaceVariant = Color(0xFF44464F),
    surfaceContainerLow = Color(0xFFF2F3FA),
    surfaceContainer = Color(0xFFECEDF4),
    outline = Color(0xFF74757F),
    outlineVariant = Color(0xFFC4C5D0),
)

/**
 * Dark color scheme following Material 3 design.
 * Primary, secondary, tertiary, and their variants stay bright (intentional design choice).
 */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB0C4FF),
    onPrimary = Color(0xFF012078),
    primaryContainer = Color(0xFF002C9F),
    onPrimaryContainer = Color(0xFFD9E2FF),
    secondary = Color(0xFFFFCC00),
    onSecondary = Color(0xFF1A1A00),
    secondaryContainer = Color(0xFFFFF0A0),
    onSecondaryContainer = Color(0xFF1A1400),
    tertiary = Color(0xFF5C6BC0),
    onTertiary = Color.White,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF11131A),
    onBackground = Color(0xFFE4E2E9),
    surface = Color(0xFF191C24),
    onSurface = Color(0xFFE4E2E9),
    surfaceVariant = Color(0xFF252835),
    onSurfaceVariant = Color(0xFFC5C6D0),
    surfaceContainerLow = Color(0xFF1A1D26),
    surfaceContainer = Color(0xFF1F2230),
    outline = Color(0xFF8E909A),
    outlineVariant = Color(0xFF363946),
)

/**
 * HitchLog application theme.
 * Automatically switches between light and dark themes based on system setting.
 *
 * @param darkTheme Whether to use dark theme. Defaults to system setting.
 * @param content The content to theme.
 */
@Composable
fun HLTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtended else LightExtended

    CompositionLocalProvider(LocalHLExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
