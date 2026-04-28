package org.gmautostop.hitchlogmp.ui.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors

/**
 * HitchLog application theme.
 * Wraps Material3 theme with HitchLog design tokens.
 */
private val LightColorScheme = lightColorScheme(
    primary = HLColors.Primary,
    onPrimary = HLColors.OnPrimary,
    primaryContainer = HLColors.PrimaryContainer,
    onPrimaryContainer = HLColors.OnPrimaryContainer,
    secondary = HLColors.Secondary,
    onSecondary = HLColors.OnSecondary,
    tertiary = HLColors.Tertiary,
    onTertiary = HLColors.OnTertiary,
    error = HLColors.Error,
    onError = HLColors.OnError,
    errorContainer = HLColors.ErrorContainer,
    onErrorContainer = HLColors.OnErrorContainer,
    background = HLColors.Background,
    surface = HLColors.Surface,
    onSurface = HLColors.OnSurface,
    surfaceVariant = HLColors.SurfaceVariant,
    onSurfaceVariant = HLColors.OnSurfaceVariant,
    surfaceContainerLow = HLColors.SurfaceContainerLow,
    surfaceContainer = HLColors.SurfaceContainer,
    outlineVariant = HLColors.OutlineVariant,
)

@Composable
fun HLTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
