package org.gmautostop.hitchlogmp.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.history_empty
import hitchlogmp.composeapp.generated.resources.log_history_title
import hitchlogmp.composeapp.generated.resources.sort_by_record
import hitchlogmp.composeapp.generated.resources.sort_by_time
import org.gmautostop.hitchlogmp.domain.ChangeType
import org.gmautostop.hitchlogmp.ui.LogHistoryViewModel
import org.gmautostop.hitchlogmp.ui.ViewState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLEmptyState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLLoadingState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLSectionHeader
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
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
    val stateValue by viewModel.state.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()

    LogHistoryScreen(
        stateValue = stateValue,
        sortMode = sortMode,
        onSortModeChange = viewModel::setSortMode,
        navigateUp = navigateUp
    )
}

@Composable
private fun LogHistoryScreen(
    stateValue: ViewState<LogHistoryData>,
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
    navigateUp: () -> Unit
) {
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HLColors.Background)
    ) {
        HLTopBar(
            title = stringResource(Res.string.log_history_title),
            onNavigateUp = navigateUp
        )
        SegmentedToggle(
            sortMode = sortMode,
            onToggle = onSortModeChange
        )

        when (stateValue) {
            is ViewState.Loading -> HLLoadingState(modifier = Modifier.fillMaxSize())
            is ViewState.Error -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(HLSpacing.md)
            ) {
                Text(
                    text = stateValue.error.displayMessage,
                    color = HLColors.Error
                )
            }
            is ViewState.Show -> {
                val data = stateValue.value
                val isEmpty = data.byRecordGroups.isEmpty()

                if (isEmpty) {
                    HLEmptyState(
                        icon = Icons.Default.History,
                        message = stringResource(Res.string.history_empty),
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (sortMode) {
                            SortMode.BY_TIME -> {
                                items(data.byTimeGroups, key = { it.date.toString() }) { group ->
                                    FlatByTimeGroupSection(group = group)
                                }
                            }
                            SortMode.BY_RECORD -> {
                                items(data.byRecordGroups, key = { it.recordId }) { group ->
                                    RecordHistoryGroupItem(group = group)
                                }
                            }
                        }
                        item(key = "bottom_spacer") {
                            Spacer(Modifier.height(HLSpacing.xxxl))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentedToggle(sortMode: SortMode, onToggle: (SortMode) -> Unit) {
    val options = listOf(SortMode.BY_TIME to stringResource(Res.string.sort_by_time),
        SortMode.BY_RECORD to stringResource(Res.string.sort_by_record))

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)
    ) {
        options.forEachIndexed { index, (mode, label) ->
            SegmentedButton(
                selected = sortMode == mode,
                onClick = { onToggle(mode) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                label = { Text(label) }
            )
        }
    }
}

@Composable
private fun FlatByTimeGroupSection(group: DateGroupUi) {
    HLSectionHeader(text = group.dateLabel.uppercase())
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
            .background(HLColors.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, HLColors.OutlineVariant, RoundedCornerShape(12.dp))
    ) {
        Column {
            group.entries.forEachIndexed { index, entry ->
                FullChangeRow(entry = entry)
                if (index < group.entries.lastIndex) {
                    HorizontalDivider(color = HLColors.OutlineVariant)
                }
            }
        }
    }
}

@Composable
private fun FullChangeRow(entry: LogHistoryEntryUi) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 72.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RecordIconChip(
            type = entry.recordType,
            size = 36.dp,
            dimmed = entry.changeType == ChangeType.DELETE
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(entry.recordType.toStringResource()),
                    style = HLTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = HLColors.OnSurface
                )
                ChangeTypeChip(changeType = entry.changeType, compact = true)
            }
            InlineDiff(before = entry.before, after = entry.after, changeType = entry.changeType)
        }
        Text(
            text = entry.formattedEditedAt,
            style = HLTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = HLColors.OnSurfaceVariant
        )
    }
}

@Composable
private fun RecordHistoryGroupItem(group: RecordGroupUi) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)
            .background(HLColors.Surface, RoundedCornerShape(16.dp))
            .border(1.dp, HLColors.OutlineVariant, RoundedCornerShape(16.dp))
    ) {
        Column {
            // Group header
            val headerBg = if (group.isDeleted) HLColors.ErrorContainer else HLColors.SurfaceContainerLow
            val headerFg = if (group.isDeleted) HLColors.OnErrorContainer else HLColors.OnSurface

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBg, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RecordIconChip(type = group.liveType, size = 36.dp)
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(group.liveType.toStringResource()),
                            style = HLTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = headerFg
                        )
                        if (group.formattedOriginalTime != null) {
                            Text(
                                text = "· ${group.formattedOriginalTime}",
                                style = HLTypography.labelMedium,
                                color = headerFg.copy(alpha = 0.7f)
                            )
                        }
                    }
                    if (group.liveText.isNotEmpty()) {
                        Text(
                            text = group.liveText,
                            style = HLTypography.bodyMedium,
                            color = headerFg.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Edit-count pill
                Box(
                    modifier = Modifier
                        .background(HLColors.PrimaryContainer, RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.entries.size.toString(),
                        color = HLColors.OnPrimaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Per-entry rows
            group.entries.forEach { entry ->
                HorizontalDivider(color = HLColors.OutlineVariant)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChangeTypeChip(changeType = entry.changeType, compact = true)
                        Text(
                            text = entry.formattedEditedAt,
                            style = HLTypography.bodySmall,
                            color = HLColors.OnSurfaceVariant
                        )
                    }
                    InlineDiff(
                        before = entry.before,
                        after = entry.after,
                        changeType = entry.changeType
                    )
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun LogHistoryScreenPreview(
    @PreviewParameter(LogHistoryDataProvider::class) data: LogHistoryData
) {
    HLTheme {
        LogHistoryScreen(
            stateValue = ViewState.Show(data),
            sortMode = SortMode.BY_TIME,
            onSortModeChange = {},
            navigateUp = {}
        )
    }
}

@Preview
@Composable
private fun FullChangeRowPreview(
    @PreviewParameter(LogHistoryEntryUiProvider::class) entry: LogHistoryEntryUi
) {
    HLTheme {
        FullChangeRow(entry = entry)
    }
}

@Preview
@Composable
private fun RecordHistoryGroupItemPreview(
    @PreviewParameter(RecordGroupUiProvider::class) group: RecordGroupUi
) {
    HLTheme {
        RecordHistoryGroupItem(group = group)
    }
}
