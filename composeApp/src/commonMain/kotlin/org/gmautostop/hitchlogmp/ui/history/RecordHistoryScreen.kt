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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.change_type_create
import hitchlogmp.composeapp.generated.resources.change_type_delete
import hitchlogmp.composeapp.generated.resources.change_type_update
import hitchlogmp.composeapp.generated.resources.history_empty
import hitchlogmp.composeapp.generated.resources.history_field_text
import hitchlogmp.composeapp.generated.resources.history_field_time
import hitchlogmp.composeapp.generated.resources.history_field_type
import hitchlogmp.composeapp.generated.resources.record_history_title
import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.dateTimeFormat
import org.gmautostop.hitchlogmp.domain.ChangeType
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.RecordHistoryViewModel
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
fun RecordHistoryScreen(
    viewModel: RecordHistoryViewModel,
    currentRecord: HitchLogRecord,
    navigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    RecordHistoryContent(
        state = state,
        currentRecord = currentRecord,
        onNavigateUp = navigateUp
    )
}

@Composable
private fun RecordHistoryContent(
    state: ViewState<List<HitchLogRecordHistoryEntry>>,
    currentRecord: HitchLogRecord,
    onNavigateUp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HLColors.Background)
    ) {
        HLTopBar(
            title = stringResource(Res.string.record_history_title),
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
                    HistoryList(
                        history = state.value,
                        currentRecord = currentRecord
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryList(
    history: List<HitchLogRecordHistoryEntry>,
    currentRecord: HitchLogRecord
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        // Build a list with current state as the "newest" entry
        val sortedHistory = history.sortedBy { it.editedAt }
        val allVersions = sortedHistory + listOf(null) // null represents current state

        items(
            items = allVersions,
            key = { entry -> entry?.historyId ?: "current" }
        ) { entry ->
            val index = allVersions.indexOf(entry)
            val previousEntry = if (index > 0) allVersions[index - 1] else null

            if (entry == null) {
                // Current state - compare with last history entry
                val oldVersion = previousEntry
                HistoryEntryItem(
                    editedAt = currentRecord.time, // Use record time as proxy
                    changeType = null, // Current state
                    oldTime = oldVersion?.time,
                    newTime = currentRecord.time,
                    oldType = oldVersion?.type,
                    newType = currentRecord.type,
                    oldText = oldVersion?.text,
                    newText = currentRecord.text
                )
            } else {
                // History entry - compare with previous
                val oldVersion = previousEntry
                HistoryEntryItem(
                    editedAt = entry.editedAt.localTZDateTime(),
                    changeType = entry.changeType,
                    oldTime = oldVersion?.time,
                    newTime = entry.time,
                    oldType = oldVersion?.type,
                    newType = entry.type,
                    oldText = oldVersion?.text,
                    newText = entry.text
                )
            }

            if (index < allVersions.size - 1) {
                HorizontalDivider(color = HLColors.OutlineVariant)
            }
        }
    }
}

@Composable
private fun HistoryEntryItem(
    editedAt: LocalDateTime,
    changeType: ChangeType?,
    oldTime: LocalDateTime?,
    newTime: LocalDateTime,
    oldType: HitchLogRecordType?,
    newType: HitchLogRecordType,
    oldText: String?,
    newText: String
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
                text = dateTimeFormat.format(editedAt),
                style = HLTypography.bodyMedium,
                color = HLColors.OnSurfaceVariant
            )
            
            changeType?.let {
                Text(
                    text = stringResource(
                        when (it) {
                            ChangeType.CREATE -> Res.string.change_type_create
                            ChangeType.UPDATE -> Res.string.change_type_update
                            ChangeType.DELETE -> Res.string.change_type_delete
                        }
                    ),
                    style = HLTypography.labelMedium,
                    color = if (it == ChangeType.DELETE) HLColors.Error else HLColors.Primary
                )
            }
        }

        // Diff fields
        if (oldTime != null && oldTime != newTime) {
            DiffField(
                label = stringResource(Res.string.history_field_time),
                oldValue = timeFormatForDisplay.format(oldTime),
                newValue = timeFormatForDisplay.format(newTime)
            )
        }

        if (oldType != null && oldType != newType) {
            DiffField(
                label = stringResource(Res.string.history_field_type),
                oldValue = stringResource(oldType.toStringResource()),
                newValue = stringResource(newType.toStringResource())
            )
        }

        if (oldText != null && oldText != newText) {
            DiffField(
                label = stringResource(Res.string.history_field_text),
                oldValue = oldText,
                newValue = newText
            )
        }

        // If no old values (first entry), show all fields
        if (oldTime == null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SimpleField(
                    label = stringResource(Res.string.history_field_time),
                    value = timeFormatForDisplay.format(newTime)
                )
                SimpleField(
                    label = stringResource(Res.string.history_field_type),
                    value = stringResource(newType.toStringResource())
                )
                if (newText.isNotEmpty()) {
                    SimpleField(
                        label = stringResource(Res.string.history_field_text),
                        value = newText
                    )
                }
            }
        }
    }
}

@Composable
private fun DiffField(
    label: String,
    oldValue: String,
    newValue: String
) {
    Column {
        Text(
            text = "$label:",
            style = HLTypography.labelSmall,
            color = HLColors.OnSurfaceVariant
        )
        Text(
            text = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        textDecoration = TextDecoration.LineThrough,
                        color = HLColors.OnSurfaceVariant
                    )
                ) {
                    append(oldValue)
                }
                append(" → ")
                append(newValue)
            },
            style = HLTypography.bodyMedium,
            color = HLColors.OnSurface
        )
    }
}

@Composable
private fun SimpleField(
    label: String,
    value: String
) {
    Row {
        Text(
            text = "$label: ",
            style = HLTypography.labelSmall,
            color = HLColors.OnSurfaceVariant
        )
        Text(
            text = value,
            style = HLTypography.bodyMedium,
            color = HLColors.OnSurface
        )
    }
}
