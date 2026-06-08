package org.gmautostop.hitchlogmp.ui.hitchlog

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.chronicle_empty
import hitchlogmp.composeapp.generated.resources.different_record_type
import hitchlogmp.composeapp.generated.resources.edit_chronicle
import hitchlogmp.composeapp.generated.resources.export_csv
import hitchlogmp.composeapp.generated.resources.export_error
import hitchlogmp.composeapp.generated.resources.export_html
import hitchlogmp.composeapp.generated.resources.export_preparing
import hitchlogmp.composeapp.generated.resources.export_text
import hitchlogmp.composeapp.generated.resources.export_title
import hitchlogmp.composeapp.generated.resources.export_xlsx
import hitchlogmp.composeapp.generated.resources.history_menu
import hitchlogmp.composeapp.generated.resources.new_record
import hitchlogmp.composeapp.generated.resources.start
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.export.ExportFormat
import org.gmautostop.hitchlogmp.ui.Error
import org.gmautostop.hitchlogmp.ui.ViewState
import org.gmautostop.hitchlogmp.ui.designsystem.components.ActionButtonSize
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLActionButton
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLBottomSheet
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLEmptyState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLLoadingState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.jetbrains.compose.resources.stringResource

// ── Top-level screen ─────────────────────────────────────────────────────────

@Composable
fun HitchLogScreen(
    viewModel: HitchLogViewModel,
    navigateUp: () -> Unit,
    editLog: (logId: String) -> Unit,
    createRecord: (type: HitchLogRecordType) -> Unit,
    editRecord: (id: String) -> Unit,
    navigateToLogHistory: () -> Unit
) {
    val state: ViewState<HitchLogState> by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Get string resources in composable context
    val preparingMessage = stringResource(Res.string.export_preparing)
    val errorMessageFormat = stringResource(Res.string.export_error, "")

    // Collect export events
    LaunchedEffect(Unit) {
        viewModel.exportEvents.collect { event ->
            when (event) {
                is HitchLogViewModel.ExportEvent.Preparing -> {
                    snackbarHostState.showSnackbar(preparingMessage)
                }
                is HitchLogViewModel.ExportEvent.Error -> {
                    snackbarHostState.showSnackbar(
                        errorMessageFormat.replace("%1\$s", event.message)
                    )
                }
            }
        }
    }

    when (state) {
        is ViewState.Loading -> HLLoadingState()
        is ViewState.Error   -> Error((state as ViewState.Error).error.displayMessage)
        is ViewState.Show    -> {
            val hitchLogState = (state as ViewState.Show<HitchLogState>).value
            HitchLog(
                state = hitchLogState,
                navigateUp = navigateUp,
                editLog = editLog,
                createRecord = createRecord,
                editRecord = editRecord,
                navigateToLogHistory = navigateToLogHistory,
                snackbarHostState = snackbarHostState,
                onToggleRest = { viewModel.toggleRestDisplay() },
                onExport = { format -> viewModel.export(format) },
            )
        }
    }
}

// ── Main content ─────────────────────────────────────────────────────────────

@Composable
private fun HitchLog(
    state: HitchLogState,
    navigateUp: () -> Unit,
    editLog: (logId: String) -> Unit,
    createRecord: (HitchLogRecordType) -> Unit,
    editRecord: (id: String) -> Unit,
    navigateToLogHistory: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onToggleRest: () -> Unit,
    onExport: (ExportFormat) -> Unit,
) {
    val density = LocalDensity.current
    val listState = rememberLazyListState()
    val isEmpty = state.records.isEmpty()

    var exportMenuExpanded by remember { mutableStateOf(false) }

    // Measured heights of QuickActions panel (null until first measurement)
    var collapsedHeight by remember { mutableStateOf(0.dp) }
    var expandedHeight by remember { mutableStateOf(0.dp) }

    var quickCollapsed by remember { mutableStateOf(false) }
    var sheetOpen by remember { mutableStateOf(false) }

    // Animated bottom padding based on panel height
    val animatedPanelHeight by animateDpAsState(
        targetValue = if (quickCollapsed) collapsedHeight else expandedHeight,
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
            val totalItems = 1 + groups.size * 2 // +1 for props section
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HLTopBar(
                title = state.logName,
                subtitle = state.teamId.takeIf { it.isNotEmpty() },
                onNavigateUp = navigateUp,
                actions = {
                    // History icon
                    IconButton(
                        onClick = navigateToLogHistory,
                        enabled = !isEmpty
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(Res.string.history_menu),
                            tint = if (isEmpty) {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }

                    // Export icon with menu
                    Box {
                        IconButton(
                            onClick = { exportMenuExpanded = true },
                            enabled = !isEmpty
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = stringResource(Res.string.export_title),
                                tint = if (isEmpty) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        DropdownMenu(
                            expanded = exportMenuExpanded,
                            onDismissRequest = { exportMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.export_text)) },
                                onClick = {
                                    exportMenuExpanded = false
                                    onExport(ExportFormat.Text)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.export_csv)) },
                                onClick = {
                                    exportMenuExpanded = false
                                    onExport(ExportFormat.Csv)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.export_html)) },
                                onClick = {
                                    exportMenuExpanded = false
                                    onExport(ExportFormat.Html)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(Res.string.export_xlsx)) },
                                onClick = {
                                    exportMenuExpanded = false
                                    onExport(ExportFormat.Xlsx)
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                    SummaryCard(
                        summary = state.summary,
                        onToggleRest = onToggleRest
                    )
                    Box(Modifier.weight(1f)) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            item(key = "props") {
                                ChroniclePropertiesSection(
                                    logName = state.logName,
                                    team = state.team,
                                    comment = state.comment,
                                    onEdit = { editLog(state.logId) }
                                )
                            }
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
                                Spacer(Modifier.height(if (quickCollapsed) 88.dp else 200.dp))
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

// ── Chronicle Properties Section ─────────────────────────────────────────────

@Composable
private fun ChroniclePropertiesSection(
    logName: String,
    team: String?,
    comment: String?,
    onEdit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)

        Column(Modifier.padding(start = 32.dp, end = 32.dp, top = 16.dp, bottom = 16.dp)) {
            // Title row with edit button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = logName,
                    style = HLTypography.titleMedium.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(Res.string.edit_chronicle),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Team
            if (!team.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = team,
                        style = HLTypography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Comment
            if (!comment.isNullOrBlank()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp)
                    )
                    Text(
                        text = comment,
                        style = HLTypography.bodyLarge.copy(fontWeight = FontWeight.Normal),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
        Spacer(Modifier.height(4.dp))
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

/**
 * Preview parameter provider for HitchLogScreen.
 * Provides different states: loading, empty, minimal, full, various scenarios, error.
 */
private class HitchLogStateProvider : PreviewParameterProvider<ViewState<HitchLogState>> {
    override val values = sequenceOf(
        ViewState.Loading,
        ViewState.Show(sampleHitchLogState(records = emptyList())),
        ViewState.Show(sampleHitchLogState(records = sampleMinimalRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleHitchLogRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleInCarRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleRestRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleOffsideRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleFinishedRecords())),
        ViewState.Show(sampleHitchLogState(records = sampleRetiredRecords())),
        ViewState.Error(AppError.NotFound)
    )
}

@Preview
@Composable
private fun HitchLogScreenPreview(
    @PreviewParameter(HitchLogStateProvider::class) state: ViewState<HitchLogState>
) {
    HLTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (state) {
                is ViewState.Loading -> HLLoadingState()
                is ViewState.Error -> Error(state.error.displayMessage)
                is ViewState.Show -> {
                    val hitchLogState = state.value
                    HitchLog(
                        state = hitchLogState,
                        navigateUp = {},
                        editLog = {},
                        createRecord = {},
                        editRecord = {},
                        navigateToLogHistory = {},
                        snackbarHostState = remember { SnackbarHostState() },
                        onToggleRest = {},
                        onExport = {},
                    )
                }
            }
        }
    }
}
