package org.gmautostop.hitchlogmp.ui.designsystem.tokens

import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable

/**
 * Material3 OutlinedTextField colors using HLColors tokens.
 */
@Composable
fun hlOutlinedTextFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = HLColors.OnSurface,
    unfocusedTextColor = HLColors.OnSurface,
    focusedContainerColor = HLColors.Surface,
    unfocusedContainerColor = HLColors.Surface,
    focusedIndicatorColor = HLColors.Primary,
    unfocusedIndicatorColor = HLColors.OutlineVariant,
    focusedLabelColor = HLColors.Primary,
    unfocusedLabelColor = HLColors.OnSurfaceVariant,
    cursorColor = HLColors.Primary,
    errorTextColor = HLColors.OnSurface,
    errorContainerColor = HLColors.Surface,
    errorIndicatorColor = HLColors.Error,
    errorLabelColor = HLColors.Error,
    errorCursorColor = HLColors.Error,
    errorSupportingTextColor = HLColors.Error
)

/**
 * Material3 TextField (filled) colors using HLColors tokens.
 */
@Composable
fun hlFilledTextFieldColors() = TextFieldDefaults.colors(
    focusedTextColor = HLColors.OnSurface,
    unfocusedTextColor = HLColors.OnSurface,
    focusedContainerColor = HLColors.SurfaceContainer,
    unfocusedContainerColor = HLColors.SurfaceContainer,
    focusedIndicatorColor = HLColors.Primary,
    unfocusedIndicatorColor = HLColors.OnSurfaceVariant,
    focusedLabelColor = HLColors.Primary,
    unfocusedLabelColor = HLColors.OnSurfaceVariant,
    cursorColor = HLColors.Primary,
    errorTextColor = HLColors.OnSurface,
    errorContainerColor = HLColors.SurfaceContainer,
    errorIndicatorColor = HLColors.Error,
    errorLabelColor = HLColors.Error,
    errorCursorColor = HLColors.Error,
    errorSupportingTextColor = HLColors.Error
)
