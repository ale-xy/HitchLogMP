package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Bottom sheet component backed by Material3 [ModalBottomSheet].
 *
 * Renders in its own full-window layer with a correct full-screen scrim and built-in
 * system-bar inset handling — the sheet seats against the bottom edge and the scrim
 * dims the status/navigation bar areas too.
 *
 * @param open Whether the sheet is visible
 * @param title Sheet title
 * @param onClose Close handler
 * @param content Sheet content
 * @param modifier Optional modifier applied to the sheet container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HLBottomSheet(
    open: Boolean,
    title: String,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    if (!open) return

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = modifier,
        shape = HLShapes.bottomSheet,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        // Header with title and close button
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = HLSpacing.xxl, end = HLSpacing.md, top = HLSpacing.md, bottom = HLSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = HLTypography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Закрыть",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Content
        content()
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun BottomSheetOpenPreview() {
    HLTheme {
        HLBottomSheet(
            open = true,
            title = "Новая запись",
            onClose = { },
            content = {
                Column(
                    Modifier.padding(horizontal = HLSpacing.xl),
                    verticalArrangement = Arrangement.spacedBy(HLSpacing.md)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(HLSpacing.md)
                    ) {
                        HLActionButton(
                            type = HitchLogRecordType.START,
                            size = ActionButtonSize.SHEET,
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                        HLActionButton(
                            type = HitchLogRecordType.LIFT,
                            size = ActionButtonSize.SHEET,
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                        HLActionButton(
                            type = HitchLogRecordType.CHECKPOINT,
                            size = ActionButtonSize.SHEET,
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(HLSpacing.md)
                    ) {
                        HLActionButton(
                            type = HitchLogRecordType.REST_ON,
                            size = ActionButtonSize.SHEET,
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                        HLActionButton(
                            type = HitchLogRecordType.FINISH,
                            size = ActionButtonSize.SHEET,
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                        HLActionButton(
                            type = HitchLogRecordType.FREE_TEXT,
                            size = ActionButtonSize.SHEET,
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        )
    }
}
