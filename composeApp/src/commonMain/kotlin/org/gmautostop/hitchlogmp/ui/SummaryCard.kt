package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.offside_on
import hitchlogmp.composeapp.generated.resources.rest_left
import hitchlogmp.composeapp.generated.resources.rest_used
import hitchlogmp.composeapp.generated.resources.retire
import hitchlogmp.composeapp.generated.resources.status_finished
import hitchlogmp.composeapp.generated.resources.status_in_car
import org.gmautostop.hitchlogmp.domain.LiveState
import org.gmautostop.hitchlogmp.domain.LiveStatus
import org.gmautostop.hitchlogmp.domain.formatMinutes
import org.gmautostop.hitchlogmp.timeFormat
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLStatCell
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLStatusBadge
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.viewmodel.SummaryCardState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SummaryCard(summary: SummaryCardState) {
    var showUsed by remember { mutableStateOf(true) }

    Column(
        Modifier
            .padding(start = HLSpacing.xl, end = HLSpacing.xl, top = HLSpacing.lg, bottom = HLSpacing.xs)
            .fillMaxWidth()
            .clip(HLShapes.medium)
            .background(HLColors.PrimaryContainer)
            .padding(HLSpacing.xl)
    ) {
        Row(Modifier.fillMaxWidth()) {
            HLStatCell(
                icon = Icons.Filled.DirectionsCar,
                value = "${summary.lifts}",
                modifier = Modifier.weight(1f)
            )
            HLStatCell(
                icon = Icons.Filled.LocationOn,
                value = "${summary.checkpoints}",
                modifier = Modifier.weight(1f)
            )
            HLStatCell(
                icon = Icons.Filled.Hotel,
                value = formatMinutes(summary.restMin),
                label = if (showUsed) stringResource(Res.string.rest_used) else stringResource(Res.string.rest_left),
                onClick = { showUsed = !showUsed },
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
        LiveStatus.REST    -> BadgeStyle(HLColors.SurfaceVariant, HLColors.OnSurfaceVariant, Icons.Filled.Hotel, "Отдых")
        LiveStatus.OFFSIDE -> BadgeStyle(HLColors.ErrorContainer, HLColors.OnErrorContainer, Icons.Filled.PauseCircle, stringResource(Res.string.offside_on))
        LiveStatus.FINISH  -> BadgeStyle(HLColors.Primary, HLColors.OnPrimary, Icons.Filled.Flag, stringResource(Res.string.status_finished))
        LiveStatus.RETIRE  -> BadgeStyle(HLColors.Error, HLColors.OnError, Icons.Filled.Cancel, stringResource(Res.string.retire))
    }
    val sinceLabel = state.since?.let { "· с ${timeFormat.format(it)}" }

    HLStatusBadge(
        icon = icon,
        label = label,
        backgroundColor = bg,
        foregroundColor = fg,
        subtitle = sinceLabel
    )
}

private data class BadgeStyle(
    val bg: androidx.compose.ui.graphics.Color,
    val fg: androidx.compose.ui.graphics.Color,
    val icon: ImageVector,
    val label: String
)
