package org.gmautostop.hitchlogmp.ui.designsystem.tokens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.gmautostop.hitchlogmp.domain.model.LogColor

private data class LogColorPair(val light: Color, val dark: Color)

private val palette: Map<LogColor, LogColorPair> = mapOf(
    LogColor.BLUE   to LogColorPair(Color(0xFF1A3A8F), Color(0xFF3358D4)),
    LogColor.RED    to LogColorPair(Color(0xFFC62828), Color(0xFFD32F2F)),
    LogColor.YELLOW to LogColorPair(Color(0xFFF9A825), Color(0xFFF9A825)),
    LogColor.GREEN  to LogColorPair(Color(0xFF2E7D32), Color(0xFF43A047)),
    LogColor.PURPLE to LogColorPair(Color(0xFF6A1B9A), Color(0xFF8E24AA)),
    LogColor.CYAN   to LogColorPair(Color(0xFF0288D1), Color(0xFF039BE5)),
    LogColor.BLACK  to LogColorPair(Color(0xFF212121), Color(0xFF424242)),
    LogColor.WHITE  to LogColorPair(Color(0xFFE8E8E8), Color(0xFFE0E0E0)),
)

/**
 * Resolves the appropriate Color for this LogColor based on the current theme.
 */
@Composable
fun LogColor.resolve(): Color {
    val pair = palette[this] ?: palette[LogColor.BLUE]!!
    return if (isSystemInDarkTheme()) pair.dark else pair.light
}

/**
 * Whether this color needs an outline border for visibility (true for WHITE only).
 */
val LogColor.needsOutline: Boolean
    get() = this == LogColor.WHITE
