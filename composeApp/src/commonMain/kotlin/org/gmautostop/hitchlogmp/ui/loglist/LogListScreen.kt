package org.gmautostop.hitchlogmp.ui.loglist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.cancel
import hitchlogmp.composeapp.generated.resources.create_chronicle
import hitchlogmp.composeapp.generated.resources.logout
import hitchlogmp.composeapp.generated.resources.logout_confirm
import hitchlogmp.composeapp.generated.resources.logout_message_anonymous
import hitchlogmp.composeapp.generated.resources.logout_message_regular
import hitchlogmp.composeapp.generated.resources.logout_message_unsaved
import hitchlogmp.composeapp.generated.resources.logout_title
import hitchlogmp.composeapp.generated.resources.my_logs
import hitchlogmp.composeapp.generated.resources.no_logs
import hitchlogmp.composeapp.generated.resources.sort_ascending
import hitchlogmp.composeapp.generated.resources.sort_by_creation_date
import hitchlogmp.composeapp.generated.resources.sort_by_name
import hitchlogmp.composeapp.generated.resources.sort_by_record_date
import hitchlogmp.composeapp.generated.resources.sort_descending
import hitchlogmp.composeapp.generated.resources.sort_title
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.getAppVersion
import org.gmautostop.hitchlogmp.ui.Error
import org.gmautostop.hitchlogmp.ui.ViewState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLConfirmationDialog
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLEmptyState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLLoadingState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.jetbrains.compose.resources.stringResource


@Composable
fun LogListScreen(
    viewModel: LogListViewModel,
    openLog: (id: String) -> Unit,
    createLog: () -> Unit,
    editLog: (id: String) -> Unit,
    signOut: () -> Unit,
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    when (val logsState = uiState.logsState) {
        is ViewState.Loading -> HLLoadingState()
        is ViewState.Error -> {
            Error(logsState.error.displayMessage)
        }
        is ViewState.Show<List<HitchLogUi>> -> LogListScreen(
            logs = logsState.value,
            isAnonymousUser = uiState.isAnonymousUser,
            hasPendingWrites = uiState.hasPendingWrites,
            sortConfig = uiState.sortConfig,
            onSortChanged = { viewModel.updateSort(it) },
            openLog = openLog,
            createLog = createLog,
            editLog = editLog,
            signOut = signOut,
        )
    }
}

@Composable
private fun LogListScreen(
    logs: List<HitchLogUi>,
    isAnonymousUser: Boolean,
    hasPendingWrites: Boolean,
    sortConfig: SortConfig,
    onSortChanged: (SortConfig) -> Unit,
    openLog: (id: String) -> Unit,
    createLog: () -> Unit,
    editLog: (id: String) -> Unit,
    signOut: () -> Unit,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Top Bar
            HLTopBar(
                title = stringResource(Res.string.my_logs),
                showNavigationButton = false,
                onNavigateUp = { /* not used */ },
                actions = {
                    // Sort button
                    Box {
                        IconButton(
                            onClick = { showSortMenu = !showSortMenu },
                            modifier = if (showSortMenu) {
                                Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            } else {
                                Modifier
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(Res.string.sort_title),
                                tint = if (showSortMenu) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        SortDropdownMenu(
                            expanded = showSortMenu,
                            sortConfig = sortConfig,
                            onSortChanged = { config ->
                                onSortChanged(config)
                                showSortMenu = false
                            },
                            onDismiss = { showSortMenu = false }
                        )
                    }

                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(Res.string.logout),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )

            // Content
            if (logs.isEmpty()) {
                HLEmptyState(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    message = stringResource(Res.string.no_logs),
                    primaryAction = stringResource(Res.string.create_chronicle) to createLog
                )
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        top = 8.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 120.dp
                    )
                ) {
                    items(
                        items = logs,
                        key = { it.id }
                    ) { chronicle ->
                        ChronicleCard(
                            chronicle = chronicle,
                            onOpen = { openLog(chronicle.id) },
                            onEdit = { editLog(chronicle.id) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = createLog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp)
                .size(56.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.create_chronicle),
                modifier = Modifier.size(24.dp)
            )
        }

        // Version text at bottom-left (Android only)
        getAppVersion()?.let { version ->
            Text(
                text = version,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 8.dp),
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )
        }

        // Logout Dialog
        HLConfirmationDialog(
            visible = showLogoutDialog,
            onDismiss = { showLogoutDialog = false },
            title = stringResource(Res.string.logout_title),
            message = stringResource(
                when {
                    !isAnonymousUser && hasPendingWrites -> Res.string.logout_message_unsaved
                    isAnonymousUser -> Res.string.logout_message_anonymous
                    else -> Res.string.logout_message_regular
                }
            ),
            confirmLabel = stringResource(Res.string.logout_confirm),
            cancelLabel = stringResource(Res.string.cancel),
            onConfirm = signOut,
            icon = Icons.AutoMirrored.Filled.Logout,
            isDestructive = true
        )
    }
}

// ── Sort Dropdown Menu ──────────────────────────────────────────────────────

@Composable
private fun SortDropdownMenu(
    expanded: Boolean,
    sortConfig: SortConfig,
    onSortChanged: (SortConfig) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(220.dp)
    ) {
        // Header
        Text(
            text = stringResource(Res.string.sort_title),
            style = HLTypography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 14.dp, end = 8.dp, top = 8.dp, bottom = 4.dp)
        )

        SortMenuItem(
            icon = Icons.Default.CalendarToday,
            label = stringResource(Res.string.sort_by_creation_date),
            field = SortField.CREATION_DATE,
            currentConfig = sortConfig,
            onSelect = onSortChanged
        )

        SortMenuItem(
            icon = Icons.Default.Schedule,
            label = stringResource(Res.string.sort_by_record_date),
            field = SortField.RECORD_DATE,
            currentConfig = sortConfig,
            onSelect = onSortChanged
        )

        SortMenuItem(
            icon = Icons.Default.SortByAlpha,
            label = stringResource(Res.string.sort_by_name),
            field = SortField.NAME,
            currentConfig = sortConfig,
            onSelect = onSortChanged
        )
    }
}

@Composable
private fun SortMenuItem(
    icon: ImageVector,
    label: String,
    field: SortField,
    currentConfig: SortConfig,
    onSelect: (SortConfig) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = HLTypography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            DirectionButton(
                icon = Icons.Default.ArrowDownward,
                contentDescription = stringResource(Res.string.sort_descending),
                isActive = currentConfig.field == field && currentConfig.direction == SortDirection.DESCENDING,
                onClick = { onSelect(SortConfig(field, SortDirection.DESCENDING)) }
            )
            DirectionButton(
                icon = Icons.Default.ArrowUpward,
                contentDescription = stringResource(Res.string.sort_ascending),
                isActive = currentConfig.field == field && currentConfig.direction == SortDirection.ASCENDING,
                onClick = { onSelect(SortConfig(field, SortDirection.ASCENDING)) }
            )
        }
    }
}

@Composable
private fun DirectionButton(
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .then(
                if (isActive) {
                    Modifier.background(MaterialTheme.colorScheme.primary)
                } else {
                    Modifier.border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

private class LogListStatePreviewProvider : PreviewParameterProvider<LogListUiState> {
    override val values: Sequence<LogListUiState> = sequenceOf(
        LogListUiState(
            logsState = ViewState.Loading,
            isAnonymousUser = false,
            hasPendingWrites = false
        ),
        LogListUiState(
            logsState = ViewState.Show(emptyList()),
            isAnonymousUser = true,
            hasPendingWrites = false
        ),
        LogListUiState(
            logsState = ViewState.Show(emptyList()),
            isAnonymousUser = false,
            hasPendingWrites = false
        ),
        LogListUiState(
            logsState = ViewState.Show(
                listOf(
                    HitchLogUi(
                        id = "1",
                        name = "Москва → Санкт-Петербург",
                        formattedDate = "5.05.2026",
                        formattedStartDate = "15 мая 2025"
                    ),
                    HitchLogUi(
                        id = "2",
                        name = "Казань → Екатеринбург",
                        formattedDate = "15.04.2026",
                        formattedStartDate = "3 апр 2025"
                    ),
                    HitchLogUi(
                        id = "3",
                        name = "Новосибирск → Владивосток",
                        formattedDate = "1.03.2026"
                    )
                )
            ),
            isAnonymousUser = false,
            hasPendingWrites = false
        ),
        LogListUiState(
            logsState = ViewState.Error(AppError.NetworkError("Не удалось загрузить логи")),
            isAnonymousUser = false,
            hasPendingWrites = false
        )
    )
}

@Preview
@Composable
private fun LogListScreenPreview(
    @PreviewParameter(LogListStatePreviewProvider::class) uiState: LogListUiState
) {
    HLTheme {
        when (val logsState = uiState.logsState) {
            is ViewState.Loading -> HLLoadingState()
            is ViewState.Error -> {
                Error(logsState.error.displayMessage)
            }
            is ViewState.Show<List<HitchLogUi>> -> LogListScreen(
                logs = logsState.value,
                isAnonymousUser = uiState.isAnonymousUser,
                hasPendingWrites = uiState.hasPendingWrites,
                sortConfig = uiState.sortConfig,
                onSortChanged = {},
                openLog = {},
                createLog = {},
                editLog = {},
                signOut = {}
            )
        }
    }
}
