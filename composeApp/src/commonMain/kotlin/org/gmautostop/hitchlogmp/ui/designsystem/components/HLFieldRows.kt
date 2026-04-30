package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.add
import hitchlogmp.composeapp.generated.resources.subtract
import hitchlogmp.composeapp.generated.resources.time_shortcut_minus_10
import hitchlogmp.composeapp.generated.resources.time_shortcut_minus_5
import hitchlogmp.composeapp.generated.resources.time_shortcut_now
import hitchlogmp.composeapp.generated.resources.time_shortcut_plus_10
import hitchlogmp.composeapp.generated.resources.time_shortcut_plus_5
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.jetbrains.compose.resources.stringResource

/**
 * Common stepper field row component with icon, content, and stepper buttons.
 * Ensures consistent layout and alignment between date and time fields.
 *
 * @param icon Leading icon
 * @param onSubtract Callback for subtract button
 * @param onAdd Callback for add button
 * @param showDivider Whether to show bottom divider
 * @param modifier Optional modifier
 * @param content Content to display between icon and steppers
 */
@Composable
private fun StepperFieldRow(
    icon: ImageVector,
    onSubtract: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = true,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(HLColors.Surface)
                .padding(horizontal = 20.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = HLColors.OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                content()
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HLStepperButton(
                    icon = Icons.Default.Remove,
                    onClick = onSubtract,
                    size = StepperButtonSize.MEDIUM,
                    contentDescription = stringResource(Res.string.subtract)
                )
                HLStepperButton(
                    icon = Icons.Default.Add,
                    onClick = onAdd,
                    size = StepperButtonSize.MEDIUM,
                    contentDescription = stringResource(Res.string.add)
                )
            }
        }
        if (showDivider) {
            HorizontalDivider(color = HLColors.OutlineVariant, thickness = 1.dp)
        }
    }
}

/**
 * Date field row with icon, formatted date value, and stepper buttons.
 * Used in EditRecordScreen for date adjustment.
 *
 * @param dateText Current date text (dd.MM.yyyy format)
 * @param onSubtract Callback for subtracting one day
 * @param onAdd Callback for adding one day
 * @param modifier Optional modifier
 */
@Composable
fun DateFieldRow(
    dateText: String,
    onDateChange: (String) -> Unit,
    onSubtract: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    StepperFieldRow(
        icon = Icons.Default.CalendarToday,
        onSubtract = onSubtract,
        onAdd = onAdd,
        modifier = modifier
    ) {
        BasicTextField(
            value = dateText,
            onValueChange = onDateChange,
            textStyle = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                color = HLColors.OnSurface
            ),
            cursorBrush = SolidColor(HLColors.Primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

/**
 * Time field row with icon, large editable time value, steppers, and shortcut rail.
 * Used in EditRecordScreen for time adjustment with keyboard input.
 *
 * @param timeText Current time text (HH:mm format)
 * @param timeError Optional error message
 * @param onTimeChange Callback when time text changes
 * @param onSubtract Callback for subtracting one minute
 * @param onAdd Callback for adding one minute
 * @param onShortcut Callback for shortcut buttons (minutes to adjust, 0 = now)
 * @param modifier Optional modifier
 */
@Composable
fun TimeFieldRow(
    timeText: String,
    timeError: String?,
    onTimeChange: (String) -> Unit,
    onSubtract: () -> Unit,
    onAdd: () -> Unit,
    onShortcut: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HLColors.Surface)
    ) {
        StepperFieldRow(
            icon = Icons.Default.Schedule,
            onSubtract = onSubtract,
            onAdd = onAdd,
            showDivider = false,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            BasicTextField(
                value = timeText,
                onValueChange = onTimeChange,
                textStyle = TextStyle(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Medium,
                    color = HLColors.OnSurface
                ),
                cursorBrush = SolidColor(HLColors.Primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
        
        if (timeError != null) {
            Text(
                text = timeError,
                style = HLTypography.bodySmall,
                color = HLColors.Error,
                modifier = Modifier.padding(start = 60.dp, top = 4.dp, bottom = 4.dp)
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp, end = 20.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HLShortcutChip(
                label = stringResource(Res.string.time_shortcut_minus_10),
                onClick = { onShortcut(-10) },
                modifier = Modifier.weight(1f)
            )
            HLShortcutChip(
                label = stringResource(Res.string.time_shortcut_minus_5),
                onClick = { onShortcut(-5) },
                modifier = Modifier.weight(1f)
            )
            HLShortcutChip(
                label = stringResource(Res.string.time_shortcut_now),
                onClick = { onShortcut(0) },
                isPrimary = true,
                modifier = Modifier.weight(0.8f)
            )
            HLShortcutChip(
                label = stringResource(Res.string.time_shortcut_plus_5),
                onClick = { onShortcut(5) },
                modifier = Modifier.weight(1f)
            )
            HLShortcutChip(
                label = stringResource(Res.string.time_shortcut_plus_10),
                onClick = { onShortcut(10) },
                modifier = Modifier.weight(1f)
            )
        }
        
        HorizontalDivider(color = HLColors.OutlineVariant, thickness = 1.dp)
    }
}

/**
 * Note field row with icon and multiline text field.
 * Used in EditRecordScreen for free-form text input.
 *
 * @param placeholder Placeholder text (type-aware)
 * @param text Current text value
 * @param onTextChange Callback when text changes
 * @param focusRequester Optional focus requester for auto-focus
 * @param modifier Optional modifier
 */
@Composable
fun NoteFieldRow(
    placeholder: String,
    text: String,
    onTextChange: (String) -> Unit,
    focusRequester: FocusRequester? = null,
    modifier: Modifier = Modifier
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HLColors.Surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TextFields,
                contentDescription = null,
                tint = HLColors.OnSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .padding(top = 2.dp)
            )
            BasicTextField(
                value = text,
                onValueChange = { newText ->
                    onTextChange(newText)
                    coroutineScope.launch {
                        bringIntoViewRequester.bringIntoView()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 100.dp)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .then(focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
                textStyle = TextStyle(
                    fontSize = 18.sp,
                    lineHeight = 25.sp,
                    color = HLColors.OnSurface
                ),
                maxLines = Int.MAX_VALUE,
                cursorBrush = SolidColor(HLColors.Primary),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontSize = 18.sp,
                                lineHeight = 25.sp,
                                color = HLColors.OutlineVariant
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun DateFieldRowPreview() {
    HLTheme {
        Column(Modifier.padding(16.dp)) {
            DateFieldRow(
                dateText = "16.05.2026",
                onDateChange = { },
                onSubtract = { },
                onAdd = { }
            )
        }
    }
}

@Preview
@Composable
private fun TimeFieldRowPreview() {
    HLTheme {
        Column(Modifier.padding(16.dp)) {
            TimeFieldRow(
                timeText = "11:12",
                timeError = null,
                onTimeChange = { },
                onSubtract = { },
                onAdd = { },
                onShortcut = { }
            )
        }
    }
}

@Preview
@Composable
private fun TimeFieldRowWithErrorPreview() {
    HLTheme {
        Column(Modifier.padding(16.dp)) {
            TimeFieldRow(
                timeText = "25:99",
                timeError = "Неверный формат времени",
                onTimeChange = { },
                onSubtract = { },
                onAdd = { },
                onShortcut = { }
            )
        }
    }
}

@Preview
@Composable
private fun NoteFieldRowPreview() {
    HLTheme {
        Column(Modifier.padding(16.dp)) {
            NoteFieldRow(
                placeholder = "Марка автомобиля",
                text = "",
                onTextChange = { }
            )
        }
    }
}
