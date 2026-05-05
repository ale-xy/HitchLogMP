package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
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
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.ui.components.ChronicleCard
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLConfirmationDialog
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLEmptyState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLLoadingState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
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
    openLog: (id: String) -> Unit,
    createLog: () -> Unit,
    editLog: (id: String) -> Unit,
    signOut: () -> Unit,
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()

    Box(
        Modifier
            .fillMaxSize()
            .background(HLColors.Background)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Top Bar
            HLTopBar(
                title = stringResource(Res.string.my_logs),
                showNavigationButton = false,
                onNavigateUp = { /* not used */ },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = stringResource(Res.string.logout),
                            tint = HLColors.OnSurfaceVariant
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
            containerColor = HLColors.Primary,
            contentColor = HLColors.OnPrimary,
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 3.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.create_chronicle),
                modifier = Modifier.size(24.dp)
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

// ── Previews ─────────────────────────────────────────────────────────────────

/**
 * Preview parameter provider for LogListScreen.
 * Provides different states: loading, empty (anonymous), empty (regular), filled, error.
 */
private class LogListStatePreviewProvider : PreviewParameterProvider<LogListUiState> {
    override val values: Sequence<LogListUiState> = sequenceOf(
        // Loading state
        LogListUiState(
            logsState = ViewState.Loading,
            isAnonymousUser = false,
            hasPendingWrites = false
        ),
        // Empty state - anonymous user
        LogListUiState(
            logsState = ViewState.Show(emptyList()),
            isAnonymousUser = true,
            hasPendingWrites = false
        ),
        // Empty state - regular user
        LogListUiState(
            logsState = ViewState.Show(emptyList()),
            isAnonymousUser = false,
            hasPendingWrites = false
        ),
        // Filled state - multiple logs
        LogListUiState(
            logsState = ViewState.Show(
                listOf(
                    HitchLogUi(
                        id = "1",
                        name = "Москва → Санкт-Петербург",
                        formattedDate = "5.05.2026"
                    ),
                    HitchLogUi(
                        id = "2",
                        name = "Казань → Екатеринбург",
                        formattedDate = "15.04.2026"
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
        // Error state
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
                openLog = {},
                createLog = {},
                editLog = {},
                signOut = {}
            )
        }
    }
}
