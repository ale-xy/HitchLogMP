package org.gmautostop.hitchlogmp.ui.editlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.cancel
import hitchlogmp.composeapp.generated.resources.chronicle_name_label
import hitchlogmp.composeapp.generated.resources.comment_label
import hitchlogmp.composeapp.generated.resources.comment_placeholder
import hitchlogmp.composeapp.generated.resources.delete_chronicle_message
import hitchlogmp.composeapp.generated.resources.delete_chronicle_title
import hitchlogmp.composeapp.generated.resources.delete_confirm
import hitchlogmp.composeapp.generated.resources.edit_chronicle_title
import hitchlogmp.composeapp.generated.resources.new_chronicle
import hitchlogmp.composeapp.generated.resources.save
import hitchlogmp.composeapp.generated.resources.team_label
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.ui.Error
import org.gmautostop.hitchlogmp.ui.ObserveAsEvents
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLConfirmationDialog
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLLoadingState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.components.LabeledFieldRow
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.jetbrains.compose.resources.stringResource

// ── Root Composable ──────────────────────────────────────────────────────────

@Composable
fun EditLogScreen(
    viewModel: EditLogViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLog: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is EditLogEvent.NavigateBack -> onNavigateBack()
            is EditLogEvent.NavigateToLog -> onNavigateToLog(event.logId)
        }
    }

    EditLogScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

// ── Screen Composable ────────────────────────────────────────────────────────

@Composable
fun EditLogScreen(
    state: EditLogState,
    onAction: (EditLogAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    when {
        state.isLoading -> HLLoadingState()
        state.error != null -> Error(state.error.displayMessage)
        state.log != null -> EditLogContent(
            log = state.log,
            isNewMode = state.isNewMode,
            isSaveEnabled = state.isSaveEnabled,
            showDeleteDialog = state.showDeleteDialog,
            onAction = onAction,
            onNavigateBack = onNavigateBack
        )
    }
}

// ── Content Composable ───────────────────────────────────────────────────────

@Composable
private fun EditLogContent(
    log: HitchLog,
    isNewMode: Boolean,
    isSaveEnabled: Boolean,
    showDeleteDialog: Boolean,
    onAction: (EditLogAction) -> Unit,
    onNavigateBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var nameFocused by remember { mutableStateOf(false) }
    var teamFocused by remember { mutableStateOf(false) }
    var commentFocused by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .imePadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            // Top Bar
            HLTopBar(
                title = stringResource(if (isNewMode) Res.string.new_chronicle else Res.string.edit_chronicle_title),
                onNavigateUp = onNavigateBack,
                navigationIcon = Icons.Default.Close,
                actions = {
                    if (!isNewMode) {
                        IconButton(onClick = { onAction(EditLogAction.OnShowDeleteDialog) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(Res.string.delete_confirm),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )

            // Name field row
            LabeledFieldRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                label = stringResource(Res.string.chronicle_name_label),
                value = log.name,
                onValueChange = { onAction(EditLogAction.OnNameChange(it)) },
                singleLine = false,
                isFocused = nameFocused,
                fieldModifier = Modifier
                    .focusRequester(focusRequester)
                    .onFocusChanged { nameFocused = it.isFocused }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Team field row
            LabeledFieldRow(
                icon = Icons.Default.Group,
                label = stringResource(Res.string.team_label),
                value = log.team ?: "",
                onValueChange = { onAction(EditLogAction.OnTeamChange(it)) },
                singleLine = true,
                isFocused = teamFocused,
                fieldModifier = Modifier.onFocusChanged { teamFocused = it.isFocused }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

            // Comment field row — takes remaining space
            LabeledFieldRow(
                icon = Icons.Default.Chat,
                label = stringResource(Res.string.comment_label),
                value = log.comment ?: "",
                onValueChange = { onAction(EditLogAction.OnCommentChange(it)) },
                singleLine = false,
                isFocused = commentFocused,
                placeholder = stringResource(Res.string.comment_placeholder),
                modifier = Modifier.weight(1f),
                fieldModifier = Modifier.onFocusChanged { commentFocused = it.isFocused }
            )

            // Save Button
            Box(Modifier.padding(16.dp)) {
                Button(
                    onClick = { onAction(EditLogAction.OnSaveClick) },
                    enabled = isSaveEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.save),
                        style = HLTypography.labelLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        // Delete Dialog
        HLConfirmationDialog(
            visible = showDeleteDialog,
            onDismiss = { onAction(EditLogAction.OnDismissDeleteDialog) },
            title = stringResource(Res.string.delete_chronicle_title),
            message = stringResource(Res.string.delete_chronicle_message, log.name),
            confirmLabel = stringResource(Res.string.delete_confirm),
            cancelLabel = stringResource(Res.string.cancel),
            onConfirm = { onAction(EditLogAction.OnDeleteClick) },
            icon = Icons.Default.Delete,
            isDestructive = true
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private class EditLogStatePreviewProvider : PreviewParameterProvider<EditLogState> {
    override val values: Sequence<EditLogState> = sequenceOf(
        EditLogState(
            log = null,
            isLoading = true,
            error = null,
            showDeleteDialog = false,
            isNewMode = true,
            isSaveEnabled = false
        ),
        EditLogState(
            log = HitchLog(id = "", userId = "user1", name = ""),
            isLoading = false,
            error = null,
            showDeleteDialog = false,
            isNewMode = true,
            isSaveEnabled = false
        ),
        EditLogState(
            log = HitchLog(
                id = "log1",
                userId = "user1",
                name = "Москва → Санкт-Петербург",
                team = "Иванов + Сидорова",
                comment = "Тренировочная гонка"
            ),
            isLoading = false,
            error = null,
            showDeleteDialog = false,
            isNewMode = false,
            isSaveEnabled = true
        ),
        EditLogState(
            log = HitchLog(id = "log1", userId = "user1", name = "Москва → Санкт-Петербург"),
            isLoading = false,
            error = null,
            showDeleteDialog = true,
            isNewMode = false,
            isSaveEnabled = true
        ),
        EditLogState(
            log = HitchLog(id = "log1", userId = "user1", name = "Москва → Санкт-Петербург"),
            isLoading = false,
            error = AppError.NetworkError("Ошибка сохранения"),
            showDeleteDialog = false,
            isNewMode = false,
            isSaveEnabled = true
        )
    )
}

@Preview
@Composable
private fun EditLogScreenPreview(
    @PreviewParameter(EditLogStatePreviewProvider::class) state: EditLogState
) {
    HLTheme {
        EditLogScreen(
            state = state,
            onAction = {},
            onNavigateBack = {}
        )
    }
}
