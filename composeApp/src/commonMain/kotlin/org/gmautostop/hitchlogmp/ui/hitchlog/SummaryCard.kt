package org.gmautostop.hitchlogmp.ui.hitchlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.offside_on
import hitchlogmp.composeapp.generated.resources.rest
import hitchlogmp.composeapp.generated.resources.rest_left
import hitchlogmp.composeapp.generated.resources.rest_used
import hitchlogmp.composeapp.generated.resources.retire
import hitchlogmp.composeapp.generated.resources.status_finished
import hitchlogmp.composeapp.generated.resources.status_in_car
import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.domain.logic.LiveState
import org.gmautostop.hitchlogmp.domain.logic.LiveStatus
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLStatCell
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLStatusBadge
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SummaryCard(
    summary: SummaryCardState,
    onToggleRest: () -> Unit
) {
    Column(
        Modifier
            .padding(start = HLSpacing.xl, end = HLSpacing.xl, top = HLSpacing.lg, bottom = HLSpacing.xs)
            .fillMaxWidth()
            .clip(HLShapes.medium)
            .background(HLColors.PrimaryContainer)
            .padding(HLSpacing.xl)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            HLStatCell(
                icon = Icons.Filled.DirectionsCar,
                value = "${summary.lifts}",
                modifier = Modifier.weight(0.5f)
            )
            HLStatCell(
                icon = Icons.Filled.LocationOn,
                value = "${summary.checkpoints}",
                modifier = Modifier.weight(0.5f)
            )
            HLStatCell(
                icon = Icons.Filled.Hotel,
                value = if (summary.showUsed) summary.restUsedDisplay else summary.restLeftDisplay,
                label = if (summary.showUsed) stringResource(Res.string.rest_used) else stringResource(Res.string.rest_left),
                onClick = onToggleRest,
                modifier = Modifier.weight(1f),
                align = Alignment.End
            )
        }

        if (summary.liveState != null) {
            Spacer(Modifier.height(HLSpacing.lg))
            Row(horizontalArrangement = Arrangement.spacedBy(HLSpacing.sm)) {
                LiveStatusBadge(summary.liveState)
            }
        }
    }
}

@Composable
private fun LiveStatusBadge(state: LiveState) {
    val (bg, fg, icon, label) = when (state.status) {
        LiveStatus.IN_CAR  -> BadgeStyle(HLColors.Secondary, HLColors.OnSecondary, Icons.Filled.DirectionsCar, stringResource(Res.string.status_in_car))
        LiveStatus.REST    -> BadgeStyle(HLColors.SurfaceVariant, HLColors.OnSurfaceVariant, Icons.Filled.Hotel, stringResource(Res.string.rest))
        LiveStatus.OFFSIDE -> BadgeStyle(HLColors.ErrorContainer, HLColors.OnErrorContainer, Icons.Filled.PauseCircle, stringResource(Res.string.offside_on))
        LiveStatus.FINISH  -> BadgeStyle(HLColors.Primary, HLColors.OnPrimary, Icons.Filled.Flag, stringResource(Res.string.status_finished))
        LiveStatus.RETIRE  -> BadgeStyle(HLColors.Error, HLColors.OnError, Icons.Filled.Cancel, stringResource(Res.string.retire))
    }
    val sinceLabel = state.since?.let { "· с ${timeFormatForDisplay.format(it)}" }

    HLStatusBadge(
        icon = icon,
        label = label,
        backgroundColor = bg,
        foregroundColor = fg,
        subtitle = sinceLabel
    )
}

private data class BadgeStyle(
    val bg: Color,
    val fg: Color,
    val icon: ImageVector,
    val label: String
)

// ── Previews ─────────────────────────────────────────────────────────────────

private class SummaryCardStateProvider : PreviewParameterProvider<SummaryCardState> {
    override val values: Sequence<SummaryCardState> = sequenceOf(
        // Basic state - no live status
        SummaryCardState(
            lifts = 5,
            checkpoints = 3,
            restUsedDisplay = "02:30/5",
            restLeftDisplay = "01:30/2",
            showUsed = false,
            liveState = null
        ),
        // In car state
        SummaryCardState(
            lifts = 8,
            checkpoints = 5,
            restUsedDisplay = "03:45/10",
            restLeftDisplay = "00:15/1",
            showUsed = true,
            liveState = LiveState(
                status = LiveStatus.IN_CAR,
                since = LocalDateTime(2026, 5, 13, 10, 30)
            )
        ),
        // Finished state
        SummaryCardState(
            lifts = 12,
            checkpoints = 8,
            restUsedDisplay = "04:00/5",
            restLeftDisplay = "00:00/0",
            showUsed = true,
            liveState = LiveState(
                status = LiveStatus.FINISH,
                since = LocalDateTime(2026, 5, 13, 11, 0)
            )
        ),
        // Rest state
        SummaryCardState(
            lifts = 6,
            checkpoints = 4,
            restUsedDisplay = "01:15/1",
            restLeftDisplay = "02:45/2",
            showUsed = false,
            liveState = LiveState(
                status = LiveStatus.REST,
                since = LocalDateTime(2026, 5, 13, 9, 15)
            )
        ),
        // Offside state
        SummaryCardState(
            lifts = 3,
            checkpoints = 2,
            restUsedDisplay = "00:30/1",
            restLeftDisplay = "03:30/4",
            showUsed = true,
            liveState = LiveState(
                status = LiveStatus.OFFSIDE,
                since = LocalDateTime(2026, 5, 13, 8, 45)
            )
        ),
        // Retire state
        SummaryCardState(
            lifts = 7,
            checkpoints = 5,
            restUsedDisplay = "02:00/10",
            restLeftDisplay = "02:00/20",
            showUsed = false,
            liveState = LiveState(
                status = LiveStatus.RETIRE,
                since = LocalDateTime(2026, 5, 13, 12, 30)
            )
        )
    )
}

@Preview
@Composable
private fun SummaryCardPreview(
    @PreviewParameter(SummaryCardStateProvider::class) summary: SummaryCardState
) {
    HLTheme {
        Box(Modifier.background(HLColors.Background)) {
            SummaryCard(
                summary = summary,
                onToggleRest = {}
            )
        }
    }
}
