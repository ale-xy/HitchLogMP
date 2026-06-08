package org.gmautostop.hitchlogmp.ui.recordedit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.delete
import hitchlogmp.composeapp.generated.resources.edit_record_title
import hitchlogmp.composeapp.generated.resources.history
import hitchlogmp.composeapp.generated.resources.new_record_title
import hitchlogmp.composeapp.generated.resources.save
import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.designsystem.components.DateFieldRow
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLLoadingState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLRestHintBanner
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLSaveBar
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTypeChip
import org.gmautostop.hitchlogmp.ui.designsystem.components.NoteFieldRow
import org.gmautostop.hitchlogmp.ui.designsystem.components.TimeFieldRow
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.hitchlog.sampleRecord
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditRecordScreen(
    viewModel: EditRecordViewModel,
    finish: () -> Unit,
    navigateToHistory: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            focusRequester.requestFocus()
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { finish() }
    }
    
    EditRecordContent(
        state = uiState,
        callbacks = viewModel,
        onClose = finish,
        onHistory = navigateToHistory,
        focusRequester = focusRequester
    )
}

@Composable
private fun EditRecordContent(
    state: EditRecordUiState,
    callbacks: EditRecordCallbacks,
    onClose: () -> Unit,
    onHistory: () -> Unit,
    focusRequester: FocusRequester
) {
    val isEditMode = state.record.id.isNotEmpty()
    val title = if (isEditMode) {
        stringResource(Res.string.edit_record_title)
    } else {
        stringResource(Res.string.new_record_title)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            // Top bar
            HLTopBar(
                title = title,
                onNavigateUp = onClose,
                navigationIcon = Icons.Default.Close,
                actions = {
                    if (isEditMode && state.record.edited) {
                        IconButton(onClick = onHistory) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = stringResource(Res.string.history),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (isEditMode) {
                        IconButton(onClick = { callbacks.delete() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
            
            // Loading state
            if (state.isLoading) {
                HLLoadingState(
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Scrollable content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Type chip strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        HLTypeChip(type = state.record.type)
                    }
                    
                    // REST_OFF banner (conditional)
                    if (state.restOnTime != null && state.restElapsedMinutes != null) {
                        HLRestHintBanner(
                            restOnTime = state.restOnTime,
                            elapsedMinutes = state.restElapsedMinutes
                        )
                    }
                    
                    // Date row
                    DateFieldRow(
                        dateText = state.dateText,
                        onDateChange = { callbacks.updateDate(it) },
                        onSubtract = { callbacks.adjustDate(-1) },
                        onAdd = { callbacks.adjustDate(1) }
                    )
                    
                    // Time row
                    TimeFieldRow(
                        timeValue = state.timeValue,
                        timeError = null,
                        onTimeChange = { callbacks.updateTime(it) },
                        onSubtract = { callbacks.adjustTime(-1) },
                        onAdd = { callbacks.adjustTime(1) },
                        onShortcut = { minutes ->
                            if (minutes == 0) callbacks.setTimeToNow()
                            else callbacks.adjustTime(minutes)
                        }
                    )
                    
                    // Note row
                    NoteFieldRow(
                        placeholder = stringResource(recordFieldLabel(state.record.type)),
                        text = state.record.text,
                        onTextChange = { callbacks.updateText(it) },
                        focusRequester = focusRequester,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Bottom padding to ensure content can scroll above keyboard
                    Spacer(modifier = Modifier.height(HLSpacing.xxxl))
                }
                
                // Date/Time error message - attached above save bar
                if (state.validationError != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = state.validationError,
                            style = HLTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
                
                // Save bar - fixed at bottom
                HLSaveBar(
                    label = stringResource(Res.string.save),
                    enabled = state.canSave,
                    onClick = { callbacks.save() }
                )
            }
        }
        
        // Error display (overlay)
        state.error?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(HLSpacing.md)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(HLSpacing.md)
            ) {
                Text(
                    text = error.displayMessage,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

/**
 * Preview parameter provider for EditRecordScreen.
 * Provides different states: new LIFT, existing CHECKPOINT, REST_OFF with banner, validation errors, loading.
 */
private class EditRecordStateProvider : PreviewParameterProvider<EditRecordUiState> {
    override val values = sequenceOf(
        // New LIFT record
        EditRecordUiState(
            record = sampleRecord(id = "", type = HitchLogRecordType.LIFT),
            dateText = "29.04.2026",
            timeValue = TextFieldValue("14:30"),
            validationError = null,
            isLoading = false,
            error = null,
            restOnTime = null,
            restElapsedMinutes = null,
            canSave = true
        ),
        // Existing CHECKPOINT record with delete button
        EditRecordUiState(
            record = sampleRecord(
                id = "existing-id",
                type = HitchLogRecordType.CHECKPOINT,
                text = "КП-1 Сестрорецк"
            ),
            dateText = "29.04.2026",
            timeValue = TextFieldValue("14:30"),
            validationError = null,
            isLoading = false,
            error = null,
            restOnTime = null,
            restElapsedMinutes = null,
            canSave = true
        ),
        // REST_OFF with banner
        EditRecordUiState(
            record = sampleRecord(id = "", type = HitchLogRecordType.REST_OFF),
            dateText = "29.04.2026",
            timeValue = TextFieldValue("14:30"),
            validationError = null,
            isLoading = false,
            error = null,
            restOnTime = LocalDateTime(2026, 4, 29, 13, 45),
            restElapsedMinutes = 45,
            canSave = true
        ),
        // With validation errors
        EditRecordUiState(
            record = sampleRecord(id = "", type = HitchLogRecordType.LIFT),
            dateText = "32.13.2026",
            timeValue = TextFieldValue("25:99"),
            validationError = "Неверный формат даты и времени",
            isLoading = false,
            error = null,
            restOnTime = null,
            restElapsedMinutes = null,
            canSave = false
        ),
        // Loading state
        EditRecordUiState(
            record = sampleRecord(),
            dateText = "29.04.2026",
            timeValue = TextFieldValue("14:30"),
            validationError = null,
            isLoading = true,
            error = null,
            restOnTime = null,
            restElapsedMinutes = null,
            canSave = false
        )
    )
}

@Preview
@Composable
private fun EditRecordScreenPreview(
    @PreviewParameter(EditRecordStateProvider::class) state: EditRecordUiState
) {
    HLTheme {
        EditRecordContent(
            state = state,
            callbacks = object : EditRecordCallbacks {
                override fun updateDate(date: String) {}
                override fun updateTime(time: TextFieldValue) {}
                override fun updateText(text: String) {}
                override fun adjustDate(days: Int) {}
                override fun adjustTime(minutes: Int) {}
                override fun setTimeToNow() {}
                override fun save() {}
                override fun delete() {}
            },
            onClose = { },
            onHistory = { },
            focusRequester = FocusRequester()
        )
    }
}
