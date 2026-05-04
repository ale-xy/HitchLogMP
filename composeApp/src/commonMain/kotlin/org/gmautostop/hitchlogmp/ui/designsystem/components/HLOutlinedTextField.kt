package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Material 3 outlined text field with floating label animation.
 *
 * @param value Current text value
 * @param onValueChange Called when text changes
 * @param label Label text (floats to top when focused or filled)
 * @param modifier Optional modifier
 * @param focusRequester Optional focus requester for auto-focus
 */
@Composable
fun HLOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isFloating = isFocused || value.isNotEmpty()

    val borderColor by animateColorAsState(
        targetValue = if (isFocused) HLColors.Primary else HLColors.OutlineVariant,
        animationSpec = tween(200),
        label = "borderColor"
    )

    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 1.dp,
        animationSpec = tween(200),
        label = "borderWidth"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = borderWidth,
                color = borderColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Label
        Text(
            text = label,
            style = if (isFloating) HLTypography.bodySmall else HLTypography.bodyLarge,
            color = if (isFocused) HLColors.Primary else HLColors.OnSurfaceVariant,
            modifier = Modifier.align(if (isFloating) Alignment.TopStart else Alignment.CenterStart)
                .padding(top = if (isFloating) 8.dp else 0.dp)
        )

        // Text field
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(bottom = 8.dp)
                .then(
                    if (focusRequester != null) Modifier.focusRequester(focusRequester)
                    else Modifier
                ),
            textStyle = TextStyle(
                fontSize = HLTypography.bodyLarge.fontSize,
                color = HLColors.OnSurface
            ),
            cursorBrush = SolidColor(HLColors.Primary),
            interactionSource = interactionSource,
            singleLine = true
        )
    }

    // Auto-focus if requested
    if (focusRequester != null) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun OutlinedTextFieldEmptyPreview() {
    HLTheme {
        Box(Modifier.padding(16.dp)) {
            HLOutlinedTextField(
                value = "",
                onValueChange = { },
                label = "Название хроники"
            )
        }
    }
}

@Preview
@Composable
private fun OutlinedTextFieldFilledPreview() {
    HLTheme {
        Box(Modifier.padding(16.dp)) {
            HLOutlinedTextField(
                value = "Москва → Санкт-Петербург",
                onValueChange = { },
                label = "Название хроники"
            )
        }
    }
}
