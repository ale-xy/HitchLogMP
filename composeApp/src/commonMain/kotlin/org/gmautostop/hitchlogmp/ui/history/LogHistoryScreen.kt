package org.gmautostop.hitchlogmp.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.change_type_create
import hitchlogmp.composeapp.generated.resources.change_type_delete
import hitchlogmp.composeapp.generated.resources.change_type_update
import hitchlogmp.composeapp.generated.resources.history_empty
import hitchlogmp.composeapp.generated.resources.history_field_text
import hitchlogmp.composeapp.generated.resources.history_field_time
import hitchlogmp.composeapp.generated.resources.log_history_title
import org.gmautostop.hitchlogmp.dateTimeFormat
import org.gmautostop.hitchlogmp.domain.ChangeType
import org.gmautostop.hitchlogmp.domain.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.LogHistoryViewModel
import org.gmautostop.hitchlogmp.ui.ViewState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLEmptyState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLLoadingState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.toStringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LogHistoryScreen(
    viewModel: LogHistoryViewModel,
    navigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LogHistoryContent(
        state = state,
        onNavigateUp = navigateUp
    )
}

@Composable
private fun LogHistoryContent(
    state: ViewState<List<Pair<String, HitchLogRecordHistoryEntry>>>,
    onNavigateUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HLColors.Background)
    ) {
        HLTopBar(
            title = stringResource(Res.string.log_history_title),
            onNavigateUp = onNavigateUp
        )

        when (state) {
            is ViewState.Loading -> {
                HLLoadingState(modifier = Modifier.fillMaxSize())
            }
            is ViewState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(HLSpacing.md)
                ) {
                    Text(
                        text = state.error.displayMessage,
                        color = HLColors.Error
                    )
                }
            }
            is ViewState.Show -> {
                if (state.value.isEmpty()) {
                    HLEmptyState(
                        icon = Icons.Default.History,
                        message = stringResource(Res.string.history_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LogHistoryList(history = state.value)
                }
            }
        }
    }
}

@Composable
private fun LogHistoryList(
    history: List<Pair<String, HitchLogRecordHistoryEntry>>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(history) { (recordId, entry) ->
            LogHistoryEntryItem(
                entry = entry,
                recordId = recordId
            )
            HorizontalDivider(color = HLColors.OutlineVariant)
        }
    }
}

@Composable
private fun LogHistoryEntryItem(
    entry: HitchLogRecordHistoryEntry,
    recordId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HLColors.Surface)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Header: timestamp and change type
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateTimeFormat.format(entry.editedAt.localTZDateTime()),
                style = HLTypography.bodyMedium,
                color = HLColors.OnSurfaceVariant
            )
            
            Text(
                text = stringResource(
                    when (entry.changeType) {
                        ChangeType.CREATE -> Res.string.change_type_create
                        ChangeType.UPDATE -> Res.string.change_type_update
                        ChangeType.DELETE -> Res.string.change_type_delete
                    }
                ),
                style = HLTypography.labelMedium,
                color = if (entry.changeType == ChangeType.DELETE) HLColors.Error else HLColors.Primary
            )
        }

        // Record type badge
        Text(
            text = "[${stringResource(entry.type.toStringResource())}]",
            style = HLTypography.labelMedium,
            color = HLColors.Primary
        )

        // Snapshot content
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row {
                Text(
                    text = "${stringResource(Res.string.history_field_time)}: ",
                    style = HLTypography.labelSmall,
                    color = HLColors.OnSurfaceVariant
                )
                Text(
                    text = timeFormatForDisplay.format(entry.time),
                    style = HLTypography.bodyMedium,
                    color = HLColors.OnSurface
                )
            }
            
            if (entry.text.isNotEmpty()) {
                Row {
                    Text(
                        text = "${stringResource(Res.string.history_field_text)}: ",
                        style = HLTypography.labelSmall,
                        color = HLColors.OnSurfaceVariant
                    )
                    Text(
                        text = entry.text,
                        style = HLTypography.bodyMedium,
                        color = HLColors.OnSurface
                    )
                }
            }
        }
    }
}
