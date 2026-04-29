package org.gmautostop.hitchlogmp.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.chronicle_empty
import hitchlogmp.composeapp.generated.resources.different_record_type
import hitchlogmp.composeapp.generated.resources.new_record
import hitchlogmp.composeapp.generated.resources.start
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.designsystem.components.ActionButtonSize
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLActionButton
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLBottomSheet
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLEmptyState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.preview.HitchLogStateProvider
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogState
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogViewModel
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
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    val isEmpty = state.records.isEmpty()

    // Measured heights of QuickActions panel (null until first measurement)
    var collapsedHeight by remember { mutableStateOf<Dp?>(null) }
    var expandedHeight by remember { mutableStateOf<Dp?>(null) }

    var quickCollapsed by remember { mutableStateOf(false) }
    var sheetOpen by remember { mutableStateOf(false) }

    // Animated bottom padding based on panel height
    val animatedPanelHeight by animateDpAsState(
        targetValue = if (quickCollapsed) (collapsedHeight ?: 0.dp) else (expandedHeight ?: 0.dp),
        label = "panelHeight"
    )

    // Scroll tracking for panel-expand compensation
    var wasAtBottom by remember { mutableStateOf(false) }
    var lastPanelHeight by remember { mutableStateOf(0.dp) }

    val groups = remember(state.records) {
        state.records.sortedBy { it.time }.groupBy { it.time.date }.entries.toList()
    }

    // Frame-accurate scroll compensation while panel expands
    LaunchedEffect(Unit) {
        snapshotFlow { animatedPanelHeight }
            .collect { newHeight ->
                val delta = newHeight - lastPanelHeight
                if (delta > 0.dp && wasAtBottom) {
                    listState.scroll { scrollBy(with(density) { delta.toPx() }) }
                } else if (delta <= 0.dp) {
                    wasAtBottom = false
                }
                lastPanelHeight = newHeight
            }
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
                            onToggle = {
                                wasAtBottom = !listState.canScrollForward
                                quickCollapsed = !quickCollapsed
                            },
                            onPick = { type -> createRecord(type) },
                            onMore = { sheetOpen = true },
                            onHeightMeasured = { isCollapsed, height ->
                                if (isCollapsed) {
                                    collapsedHeight = height
                                } else {
                                    expandedHeight = height
                                }
                            },
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
