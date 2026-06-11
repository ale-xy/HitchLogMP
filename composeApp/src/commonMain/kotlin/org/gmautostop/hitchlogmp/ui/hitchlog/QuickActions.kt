package org.gmautostop.hitchlogmp.ui.hitchlog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.more_actions
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.components.toStringResource
import org.gmautostop.hitchlogmp.ui.designsystem.components.ActionButtonSize
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLActionButton
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QuickActions(
    ladder: List<HitchLogRecordType>,
    collapsed: Boolean,
    onToggle: () -> Unit,
    onPick: (HitchLogRecordType) -> Unit,
    onMore: () -> Unit,
    onHeightMeasured: (collapsed: Boolean, heightDp: Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val top = ladder.getOrNull(0)
    val second = ladder.getOrNull(1)
    val medium = ladder.drop(2).take(3)

    Box(modifier.fillMaxWidth()) {
        if (collapsed) {
            if (top != null) {
                val topLabel = stringResource(top.toStringResource())
                Row(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = HLSpacing.lg, bottom = HLSpacing.lg)
                        .clip(HLShapes.large)
                        .background(MaterialTheme.colorScheme.primary)
                        .onSizeChanged { size ->
                            onHeightMeasured(true, with(density) { size.height.toDp() })
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        Modifier
                            .clickable { onPick(top) }
                            .padding(start = 18.dp, end = 14.dp, top = HLSpacing.xl, bottom = HLSpacing.xl),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            topLabel,
                            style = HLTypography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f))
                    )
                    Box(
                        Modifier
                            .size(48.dp)
                            .clickable { onToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.KeyboardArrowUp,
                            contentDescription = "Развернуть",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(start = HLSpacing.lg, end = HLSpacing.lg, top = HLSpacing.xs, bottom = HLSpacing.lg)
                    .onSizeChanged { size ->
                        onHeightMeasured(false, with(density) { size.height.toDp() })
                    }
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Свернуть",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val isSingleAction = ladder.size == 1

                if (!isSingleAction) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(HLSpacing.md)
                    ) {
                        if (top != null) {
                            HLActionButton(
                                type = top,
                                size = ActionButtonSize.BIG,
                                highlight = true,
                                onClick = { onPick(top) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (second != null) {
                            HLActionButton(
                                type = second,
                                size = ActionButtonSize.BIG,
                                onClick = { onPick(second) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(HLSpacing.md))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(HLSpacing.md)
                    ) {
                        medium.forEach { type ->
                            HLActionButton(
                                type = type,
                                size = ActionButtonSize.MEDIUM,
                                onClick = { onPick(type) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        MoreTile(
                            onClick = onMore,
                            isSquare = true,
                            size = 44.dp
                        )
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(HLSpacing.md)
                    ) {
                        if (top != null) {
                            HLActionButton(
                                type = top,
                                size = ActionButtonSize.BIG,
                                highlight = true,
                                onClick = { onPick(top) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        MoreTile(
                            onClick = onMore,
                            isSquare = true,
                            size = 64.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSquare: Boolean = false,
    size: Dp = 44.dp
) {
    Box(
        modifier
            .then(if (isSquare) Modifier.size(size) else Modifier.height(44.dp))
            .clip(HLShapes.medium)
            .background(MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Apps,
            contentDescription = stringResource(Res.string.more_actions),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

/**
 * Preview parameter provider for QuickActions states.
 */
class QuickActionsStateProvider : PreviewParameterProvider<QuickActionsState> {
    override val values: Sequence<QuickActionsState> = sequenceOf(
        // Full ladder, expanded
        QuickActionsState(
            ladder = listOf(
                HitchLogRecordType.LIFT,
                HitchLogRecordType.GET_OFF,
                HitchLogRecordType.WALK,
                HitchLogRecordType.CHECKPOINT,
                HitchLogRecordType.MEET,
                HitchLogRecordType.REST_ON,
                HitchLogRecordType.FREE_TEXT
            ),
            collapsed = false
        ),
        // Full ladder, collapsed
        QuickActionsState(
            ladder = listOf(
                HitchLogRecordType.LIFT,
                HitchLogRecordType.GET_OFF,
                HitchLogRecordType.WALK,
                HitchLogRecordType.CHECKPOINT,
                HitchLogRecordType.MEET,
                HitchLogRecordType.REST_ON,
                HitchLogRecordType.FREE_TEXT
            ),
            collapsed = true
        ),
        // Minimal ladder, expanded
        QuickActionsState(
            ladder = listOf(
                HitchLogRecordType.LIFT,
                HitchLogRecordType.WALK,
                HitchLogRecordType.CHECKPOINT
            ),
            collapsed = false
        ),
        // Minimal ladder, collapsed
        QuickActionsState(
            ladder = listOf(
                HitchLogRecordType.LIFT,
                HitchLogRecordType.WALK,
                HitchLogRecordType.CHECKPOINT
            ),
            collapsed = true
        ),
        // Single action, expanded
        QuickActionsState(
            ladder = listOf(HitchLogRecordType.LIFT),
            collapsed = false
        ),
        // Single action, collapsed
        QuickActionsState(
            ladder = listOf(HitchLogRecordType.LIFT),
            collapsed = true
        ),
        // Empty ladder, expanded
        QuickActionsState(
            ladder = emptyList(),
            collapsed = false
        ),
        // Empty ladder, collapsed
        QuickActionsState(
            ladder = emptyList(),
            collapsed = true
        )
    )
}

data class QuickActionsState(
    val ladder: List<HitchLogRecordType>,
    val collapsed: Boolean
)

@Preview
@Composable
private fun QuickActionsPreview(
    @PreviewParameter(QuickActionsStateProvider::class) state: QuickActionsState
) {
    HLTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
        ) {
            QuickActions(
                ladder = state.ladder,
                collapsed = state.collapsed,
                onToggle = { },
                onPick = { },
                onMore = { },
                onHeightMeasured = { _, _ -> }
            )
        }
    }
}
