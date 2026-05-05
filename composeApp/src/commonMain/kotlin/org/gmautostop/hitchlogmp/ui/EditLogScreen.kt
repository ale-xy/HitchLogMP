package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.cancel
import hitchlogmp.composeapp.generated.resources.chronicle_name_label
import hitchlogmp.composeapp.generated.resources.delete_chronicle_message
import hitchlogmp.composeapp.generated.resources.delete_chronicle_title
import hitchlogmp.composeapp.generated.resources.delete_confirm
import hitchlogmp.composeapp.generated.resources.edit_chronicle_title
import hitchlogmp.composeapp.generated.resources.new_chronicle
import hitchlogmp.composeapp.generated.resources.save
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLConfirmationDialog
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLLoadingState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.hlOutlinedTextFieldColors
import org.gmautostop.hitchlogmp.ui.viewmodel.EditLogAction
import org.gmautostop.hitchlogmp.ui.viewmodel.EditLogEvent
import org.gmautostop.hitchlogmp.ui.viewmodel.EditLogState
import org.gmautostop.hitchlogmp.ui.viewmodel.EditLogViewModel
import org.jetbrains.compose.resources.stringResource

// ── Root Composable ──────────────────────────────────────────────────────────

@Composable
fun EditLogScreen(
    viewModel: EditLogViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is EditLogEvent.NavigateBack -> onNavigateBack()
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

    Box(
        Modifier
            .fillMaxSize()
            .background(HLColors.Background)
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
                                tint = HLColors.Error
                            )
                        }
                    }
                }
            )

            // Content
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                // Text Field
                OutlinedTextField(
                    value = log.name,
                    onValueChange = { onAction(EditLogAction.OnNameChange(it)) },
                    label = { Text(stringResource(Res.string.chronicle_name_label)) },
                    colors = hlOutlinedTextFieldColors(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                Spacer(Modifier.height(20.dp))

                // Save Button
                Button(
                    onClick = { onAction(EditLogAction.OnSaveClick) },
                    enabled = isSaveEnabled,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HLColors.Primary,
                        contentColor = HLColors.OnPrimary,
                        disabledContainerColor = HLColors.SurfaceVariant,
                        disabledContentColor = HLColors.OnSurfaceVariant
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

@Preview
@Composable
private fun EditLogScreenNewModePreview() {
    HLTheme {
        EditLogScreen(
            state = EditLogState(
                log = HitchLog(id = "", userId = "user1", name = ""),
                isLoading = false,
                isNewMode = true,
                isSaveEnabled = false,
                showDeleteDialog = false
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun EditLogScreenEditModePreview() {
    HLTheme {
        EditLogScreen(
            state = EditLogState(
                log = HitchLog(id = "log1", userId = "user1", name = "Москва → Санкт-Петербург"),
                isLoading = false,
                isNewMode = false,
                isSaveEnabled = true,
                showDeleteDialog = false
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}

@Preview
@Composable
private fun EditLogScreenDeleteDialogPreview() {
    HLTheme {
        EditLogScreen(
            state = EditLogState(
                log = HitchLog(id = "log1", userId = "user1", name = "Москва → Санкт-Петербург"),
                isLoading = false,
                isNewMode = false,
                isSaveEnabled = true,
                showDeleteDialog = true
            ),
            onAction = {},
            onNavigateBack = {}
        )
    }
}
