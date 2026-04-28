package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.chronicle_empty
import hitchlogmp.composeapp.generated.resources.different_record_type
import hitchlogmp.composeapp.generated.resources.more_actions
import hitchlogmp.composeapp.generated.resources.new_record
import hitchlogmp.composeapp.generated.resources.offside_on
import hitchlogmp.composeapp.generated.resources.rest_left
import hitchlogmp.composeapp.generated.resources.rest_used
import hitchlogmp.composeapp.generated.resources.retire
import hitchlogmp.composeapp.generated.resources.start
import hitchlogmp.composeapp.generated.resources.status_finished
import hitchlogmp.composeapp.generated.resources.status_in_car
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.LiveState
import org.gmautostop.hitchlogmp.domain.LiveStatus
import org.gmautostop.hitchlogmp.domain.formatMinutes
import org.gmautostop.hitchlogmp.timeFormat
import org.gmautostop.hitchlogmp.ui.designsystem.components.ActionButtonSize
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLActionButton
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLBottomSheet
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLEmptyState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLStatCell
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLStatusBadge
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.preview.HitchLogStateProvider
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogState
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.SummaryCardState
import org.gmautostop.hitchlogmp.ui.viewmodel.ViewState
import org.jetbrains.compose.resources.stringResource

// ── Top-level screen ─────────────────────────────────────────────────────────

@Composable
fun HitchLogScreen(
    viewModel: HitchLogViewModel,
    navigateUp: () -> Unit,
    createRecord: (type: HitchLogRecordType) -> Unit,
    editRecord: (id: String) -> Unit,
) {
    val state: ViewState<HitchLogState> by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is ViewState.Loading -> Loading()
        is ViewState.Error   -> Error((state as ViewState.Error).error.displayMessage)
        is ViewState.Show    -> {
            val hitchLogState = (state as ViewState.Show<HitchLogState>).value
            HitchLog(
                state = hitchLogState,
                navigateUp = navigateUp,
                createRecord = createRecord,
                editRecord = editRecord,
            )
        }
    }
}

// ── Main content ─────────────────────────────────────────────────────────────

@Composable
private fun HitchLog(
    state: HitchLogState,
    navigateUp: () -> Unit,
    createRecord: (HitchLogRecordType) -> Unit,
    editRecord: (id: String) -> Unit,
) {
    val listState = rememberLazyListState()
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    val isEmpty = state.records.isEmpty()

    var quickCollapsed by remember { mutableStateOf(true) }
    var sheetOpen by remember { mutableStateOf(false) }

    val groups = remember(state.records) {
        state.records.sortedBy { it.time }.groupBy { it.time.date }.entries.toList()
    }

    LaunchedEffect(state.records.size) {
        if (state.records.isNotEmpty()) {
            val totalItems = groups.size * 2
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Scaffold(
        containerColor = HLColors.Background,
        topBar = {
            HLTopBar(
                title = state.logName,
                subtitle = state.teamId.takeIf { it.isNotEmpty() },
                scrolled = scrolled,
                onNavigateUp = navigateUp,
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = null,
                            tint = HLColors.OnSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(HLColors.Background)
        ) {
            if (isEmpty) {
                HLEmptyState(
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    message = stringResource(Res.string.chronicle_empty),
                    primaryAction = stringResource(Res.string.start) to { createRecord(HitchLogRecordType.START) },
                    secondaryAction = stringResource(Res.string.different_record_type) to { sheetOpen = true }
                )
            } else {
                Column(Modifier.fillMaxSize()) {
                    SummaryCard(summary = state.summary)
                    Box(Modifier.weight(1f)) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            groups.forEach { (date, items) ->
                                item(key = "header_${date}") {
                                    DateHeader(date = date)
                                }
                                item(key = "group_${date}") {
                                    RecordGroupCard(
                                        items = items,
                                        editRecord = editRecord,
                                    )
                                }
                            }
                            item {
                                Spacer(Modifier.height(if (quickCollapsed) 88.dp else 240.dp))
                            }
                        }
                        QuickActions(
                            ladder = state.ladder,
                            collapsed = quickCollapsed,
                            onToggle = { quickCollapsed = !quickCollapsed },
                            onPick = { type -> createRecord(type) },
                            onMore = { sheetOpen = true },
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }

            HLBottomSheet(
                open = sheetOpen,
                title = stringResource(Res.string.new_record),
                onClose = { sheetOpen = false },
                modifier = Modifier.zIndex(10f),
                content = {
                    Column(
                        Modifier.padding(horizontal = HLSpacing.xl),
                        verticalArrangement = Arrangement.spacedBy(HLSpacing.md)
                    ) {
                        HitchLogRecordType.entries.chunked(3).forEach { row ->
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(HLSpacing.md)
                            ) {
                                row.forEach { type ->
                                    HLActionButton(
                                        type = type,
                                        size = ActionButtonSize.SHEET,
                                        onClick = { createRecord(type); sheetOpen = false },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            )
        }
    }
}

// ── SummaryCard ───────────────────────────────────────────────────────────────

@Composable
private fun SummaryCard(summary: SummaryCardState) {
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

// ── QuickActions ──────────────────────────────────────────────────────────────

@Composable
private fun QuickActions(
    ladder: List<HitchLogRecordType>,
    collapsed: Boolean,
    onToggle: () -> Unit,
    onPick: (HitchLogRecordType) -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        .background(HLColors.Primary),
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
                            tint = HLColors.OnPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            topLabel,
                            style = HLTypography.labelLarge,
                            color = HLColors.OnPrimary
                        )
                    }
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(HLColors.OnPrimary.copy(alpha = 0.25f))
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
                            tint = HLColors.OnPrimary,
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
                    .background(HLColors.Surface)
                    .border(
                        width = 1.dp,
                        color = HLColors.OutlineVariant,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(start = HLSpacing.lg, end = HLSpacing.lg, bottom = HLSpacing.lg)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            Icons.Filled.KeyboardArrowDown,
                            contentDescription = "Свернуть",
                            tint = HLColors.OnSurfaceVariant
                        )
                    }
                }

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
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MoreTile(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(44.dp)
            .clip(HLShapes.medium)
            .background(HLColors.PrimaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = HLSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.Apps,
            contentDescription = null,
            tint = HLColors.Primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(HLSpacing.sm))
        Text(
            stringResource(Res.string.more_actions),
            style = HLTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = HLColors.OnPrimaryContainer
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun HitchLogScreenPreview(
    @PreviewParameter(HitchLogStateProvider::class) state: ViewState<HitchLogState>
) {
    HLTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(HLColors.Background)
        ) {
            when (state) {
                is ViewState.Loading -> Loading()
                is ViewState.Error -> Error(state.error.displayMessage)
                is ViewState.Show -> {
                    val hitchLogState = state.value
                    HitchLog(
                        state = hitchLogState,
                        navigateUp = {},
                        createRecord = {},
                        editRecord = {}
                    )
                }
            }
        }
    }
}
