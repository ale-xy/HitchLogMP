package org.gmautostop.hitchlogmp.ui.designsystem.tokens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.gmautostop.hitchlogmp.domain.model.LogColor

/**
 * Raw color swatches shared by the record-type palette and the chronicle (LogColor)
 * palette. Base name = light-theme variant; the `Dark` suffix = dark-theme variant.
 * Several hues are intentionally shared between the two palettes.
 */
private object PaletteTokens {
    val Blue = Color(0xFF1A3A8F)
    val BlueDark = Color(0xFF3358D4)
    val Red = Color(0xFFC62828)
    val RedDark = Color(0xFFD32F2F)
    val Yellow = Color(0xFFFFC400)
    val Orange = Color(0xFFE65100)
    val OrangeDark = Color(0xFFFB8C00)
    val Green = Color(0xFF2E7D32)
    val GreenDark = Color(0xFF43A047)
    val Magenta = Color(0xFFAD1457)
    val MagentaDark = Color(0xFFD81B60)
    val Purple = Color(0xFF6A1B9A)
    val PurpleDark = Color(0xFF8E24AA)
    val Cyan = Color(0xFF0288D1)
    val CyanDark = Color(0xFF039BE5)
    val Charcoal = Color(0xFF212121) // record NEUTRAL_DARK / chronicle BLACK
    val CharcoalDark = Color(0xFF424242)
    val Silver = Color(0xFFE8E8E8) // record NEUTRAL_LIGHT / chronicle WHITE
    val SilverDark = Color(0xFFE0E0E0)

    // Foreground (on-) colors
    val OnLight = Color(0xFFFFFFFF) // white text on saturated/dark hues
    val OnYellow = Color(0xFF1A1400)
    val OnOrangeDark = Color(0xFF2A1500)
    val OnSilver = Color(0xFF1A1B20)
}

// ── Record-type palette ────────────────────────────────────────────────────

/**
 * Explicit record-type color palette (per design/project/hitchlog-data.jsx).
 * Each hue carries a light + dark background and the matching on-color.
 * Dark values stay fully saturated (not pastel).
 */
enum class RecordColor { BLUE, YELLOW, ORANGE, GREEN, MAGENTA, PURPLE, NEUTRAL_DARK, NEUTRAL_LIGHT }

private data class RecordColorSpec(
    val lightBg: Color,
    val darkBg: Color,
    val lightOn: Color,
    val darkOn: Color,
)

private val recordPalette: Map<RecordColor, RecordColorSpec> = mapOf(
    RecordColor.BLUE          to RecordColorSpec(PaletteTokens.Blue, PaletteTokens.BlueDark, PaletteTokens.OnLight, PaletteTokens.OnLight),
    RecordColor.YELLOW        to RecordColorSpec(PaletteTokens.Yellow, PaletteTokens.Yellow, PaletteTokens.OnYellow, PaletteTokens.OnYellow),
    RecordColor.ORANGE        to RecordColorSpec(PaletteTokens.Orange, PaletteTokens.OrangeDark, PaletteTokens.OnLight, PaletteTokens.OnOrangeDark),
    RecordColor.GREEN         to RecordColorSpec(PaletteTokens.Green, PaletteTokens.GreenDark, PaletteTokens.OnLight, PaletteTokens.OnLight),
    RecordColor.MAGENTA       to RecordColorSpec(PaletteTokens.Magenta, PaletteTokens.MagentaDark, PaletteTokens.OnLight, PaletteTokens.OnLight),
    RecordColor.PURPLE        to RecordColorSpec(PaletteTokens.Purple, PaletteTokens.PurpleDark, PaletteTokens.OnLight, PaletteTokens.OnLight),
    RecordColor.NEUTRAL_DARK  to RecordColorSpec(PaletteTokens.Charcoal, PaletteTokens.CharcoalDark, PaletteTokens.OnLight, PaletteTokens.OnLight),
    RecordColor.NEUTRAL_LIGHT to RecordColorSpec(PaletteTokens.Silver, PaletteTokens.SilverDark, PaletteTokens.OnSilver, PaletteTokens.OnSilver),
)

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
 * Resolves the [ChipColors] for a record color in the current theme.
 *
 * Closing-half ("end") events render as an outlined contour chip: a surface
 * background with the hue applied to the icon and border. Opening events render
 * as a solid chip.
 */
@Composable
fun recordChipColors(color: RecordColor, end: Boolean): ChipColors {
    val spec = recordPalette.getValue(color)
    val dark = isSystemInDarkTheme()
    val base = if (dark) spec.darkBg else spec.lightBg
    val on = if (dark) spec.darkOn else spec.lightOn
    return if (end) {
        ChipColors(bg = MaterialTheme.colorScheme.surface, fg = base, stroke = base)
    } else {
        ChipColors(bg = base, fg = on)
    }
}

// ── Chronicle (LogColor) palette ─────────────────────────────────────────────

private data class LogColorPair(val light: Color, val dark: Color)

private val logPalette: Map<LogColor, LogColorPair> = mapOf(
    LogColor.BLUE   to LogColorPair(PaletteTokens.Blue, PaletteTokens.BlueDark),
    LogColor.RED    to LogColorPair(PaletteTokens.Red, PaletteTokens.RedDark),
    LogColor.YELLOW to LogColorPair(PaletteTokens.Yellow, PaletteTokens.Yellow),
    LogColor.GREEN  to LogColorPair(PaletteTokens.Green, PaletteTokens.GreenDark),
    LogColor.PURPLE to LogColorPair(PaletteTokens.Purple, PaletteTokens.PurpleDark),
    LogColor.CYAN   to LogColorPair(PaletteTokens.Cyan, PaletteTokens.CyanDark),
    LogColor.BLACK  to LogColorPair(PaletteTokens.Charcoal, PaletteTokens.CharcoalDark),
    LogColor.WHITE  to LogColorPair(PaletteTokens.Silver, PaletteTokens.SilverDark),
)

/**
 * Resolves the appropriate Color for this LogColor based on the current theme.
 */
@Composable
fun LogColor.resolve(): Color {
    val pair = logPalette[this] ?: logPalette.getValue(LogColor.BLUE)
    return if (isSystemInDarkTheme()) pair.dark else pair.light
}

/**
 * Whether this color needs an outline border for visibility (true for WHITE only).
 */
val LogColor.needsOutline: Boolean
    get() = this == LogColor.WHITE
