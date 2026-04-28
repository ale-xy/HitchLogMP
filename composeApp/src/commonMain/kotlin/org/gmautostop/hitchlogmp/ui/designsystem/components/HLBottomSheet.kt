package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Bottom sheet component with scrim overlay.
 * Features rounded top corners, drag handle, title, and close button.
 *
 * @param open Whether the sheet is visible
 * @param title Sheet title
 * @param onClose Close handler
 * @param content Sheet content
 * @param modifier Optional modifier
 */
@Composable
fun HLBottomSheet(
    open: Boolean,
    title: String,
    onClose: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        if (open) {
            // Scrim overlay
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.32f))
                    .clickable(onClick = onClose)
            )
        }

        if (open) {
            Column(
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clip(HLShapes.bottomSheet)
                    .background(HLColors.SurfaceContainerLow)
                    .padding(bottom = 20.dp)
            ) {
                // Drag handle
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = HLSpacing.lg, bottom = HLSpacing.xs)
                        .size(width = 32.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(HLColors.OutlineVariant)
                )

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
                        color = HLColors.OnSurface
                    )
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Закрыть",
                            tint = HLColors.OnSurfaceVariant
                        )
                    }
                }

                // Content
                content()
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun BottomSheetOpenPreview() {
    HLTheme {
        Box(Modifier.fillMaxSize().background(HLColors.Background)) {
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
}
