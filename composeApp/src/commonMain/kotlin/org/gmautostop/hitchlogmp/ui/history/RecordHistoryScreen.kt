package org.gmautostop.hitchlogmp.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.history_current_state
import hitchlogmp.composeapp.generated.resources.history_deleted_record
import hitchlogmp.composeapp.generated.resources.history_empty
import hitchlogmp.composeapp.generated.resources.history_field_text
import hitchlogmp.composeapp.generated.resources.history_field_time
import hitchlogmp.composeapp.generated.resources.history_field_type
import hitchlogmp.composeapp.generated.resources.record_history_title
import org.gmautostop.hitchlogmp.domain.ChangeType
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.ViewState
import org.gmautostop.hitchlogmp.ui.components.toStringResource
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLEmptyState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLLoadingState
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLTopBar
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecordHistoryScreen(
    viewModel: RecordHistoryViewModel,
    navigateUp: () -> Unit
) {
    val stateValue by viewModel.state.collectAsStateWithLifecycle()
    val currentRecord by viewModel.currentRecordUi.collectAsStateWithLifecycle()

    RecordHistoryScreen(
        stateValue = stateValue,
        currentRecord = currentRecord,
        navigateUp = navigateUp
    )
}

@Composable
private fun RecordHistoryScreen(
    stateValue: ViewState<List<RecordVersionUi>>,
    currentRecord: CurrentRecordUi?,
    navigateUp: () -> Unit
) {
    val listState = rememberLazyListState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HLColors.Background)
    ) {
        HLTopBar(
            title = stringResource(Res.string.record_history_title),
            onNavigateUp = navigateUp
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
                val versions = stateValue.value
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    currentRecord?.let { record ->
                        item(key = "current_state") {
                            CurrentStateCard(record = record)
                        }
                    }
                    if (versions.isEmpty()) {
                        item(key = "empty") {
                            HLEmptyState(
                                icon = Icons.Default.History,
                                message = stringResource(Res.string.history_empty),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = HLSpacing.xxl)
                            )
                        }
                    } else {
                        items(versions, key = { it.historyId }) { version ->
                            VersionCard(version = version)
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

@Composable
private fun CurrentStateCard(record: CurrentRecordUi) {
    val bg = if (record.isDeleted) HLColors.ErrorContainer else HLColors.PrimaryContainer
    val fg = if (record.isDeleted) HLColors.OnErrorContainer else HLColors.OnPrimaryContainer
    val strike = if (record.isDeleted) TextDecoration.LineThrough else null
    val labelText = stringResource(
        if (record.isDeleted) Res.string.history_deleted_record else Res.string.history_current_state
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = labelText.uppercase(),
                style = HLTypography.labelSmall,
                color = HLColors.OnSurface.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RecordIconChip(type = record.type, size = 40.dp)
                Column {
                    Text(
                        text = stringResource(record.type.toStringResource()),
                        style = HLTypography.titleMedium,
                        color = fg,
                        textDecoration = strike
                    )
                    Text(
                        text = record.formattedTime,
                        style = HLTypography.bodyMedium,
                        color = fg,
                        textDecoration = strike
                    )
                }
            }
            if (record.text.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = record.text,
                    style = HLTypography.bodyMedium,
                    color = fg,
                    textDecoration = strike
                )
            }
        }
    }
}

@Composable
private fun DiffBody(before: RecordFields?, after: RecordFields?, changeType: ChangeType) {
    fun timeOld(f: RecordFields): @Composable () -> Unit = {
        Text(f.formattedTime, style = HLTypography.bodyMedium,
            color = HLColors.Error, textDecoration = TextDecoration.LineThrough)
    }
    fun timeNew(f: RecordFields): @Composable () -> Unit = {
        Text(f.formattedTime, style = HLTypography.bodyMedium, color = HLColors.OnSurface)
    }
    fun textOld(text: String): @Composable () -> Unit = {
        Text(text, style = HLTypography.bodyMedium, color = HLColors.Error, textDecoration = TextDecoration.LineThrough)
    }
    fun textNew(text: String): @Composable () -> Unit = {
        Text(text, style = HLTypography.bodyMedium, color = HLColors.OnSurface)
    }

    val timeLabel = stringResource(Res.string.history_field_time)
    val typeLabel = stringResource(Res.string.history_field_type)
    val textLabel = stringResource(Res.string.history_field_text)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when (changeType) {
            ChangeType.CREATE -> after?.let { f ->
                DiffPairRow(timeLabel,
                    old = null,
                    new = timeNew(f))
                DiffPairRow(typeLabel,
                    old = null,
                    new = { TypePill(f.type) })
                f.text?.let {
                    DiffPairRow(textLabel,
                        old = null,
                        new = textNew(it))
                }
            }
            ChangeType.DELETE -> before?.let { f ->
                DiffPairRow(timeLabel,
                    old = timeOld(f),
                    new = null)
                DiffPairRow(typeLabel,
                    old = { TypePill(f.type, strike = true) },
                    new = null)
                f.text?.let {
                    DiffPairRow(textLabel,
                        old = textOld(it),
                        new = null)
                }
            }
            ChangeType.UPDATE -> {
                if (before?.time != after?.time)
                    DiffPairRow(timeLabel,
                        old = before?.let { timeOld(it) },
                        new = after?.let { timeNew(it) })
                if (before?.type != after?.type)
                    DiffPairRow(typeLabel,
                        old = before?.let { { TypePill(it.type, strike = true) } },
                        new = after?.let { { TypePill(it.type) } })
                if (before?.text != after?.text)
                    DiffPairRow(textLabel,
                        old = before?.text?.let { textOld(it) },
                        new = after?.text?.let { textNew(it) })
            }
        }
    }
}

@Composable
private fun DiffPairRow(
    label: String,
    old: (@Composable () -> Unit)?,
    new: (@Composable () -> Unit)?
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.uppercase(),
            style = HLTypography.labelMedium,
            color = HLColors.OnSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (old != null) old()
            if (new != null) new()
        }
    }
}

@Composable
private fun VersionCard(version: RecordVersionUi) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = HLColors.Surface
    ) {
        Box(
            modifier = Modifier.border(1.dp, HLColors.OutlineVariant, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ChangeTypeChip(version.changeType)
                    Text(
                        text = version.formattedEditedAt,
                        style = HLTypography.bodyMedium,
                        color = HLColors.OnSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
                DiffBody(version.before, version.after, version.changeType)
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun RecordHistoryScreenPreview(
    @PreviewParameter(RecordVersionUiProvider::class) version: RecordVersionUi
) {
    HLTheme {
        RecordHistoryScreen(
            stateValue = ViewState.Show(listOf(version)),
            currentRecord = CurrentRecordUi(
                type = HitchLogRecordType.LIFT,
                formattedTime = "14:30",
                text = "Попутчик из Москвы",
                isDeleted = false
            ),
            navigateUp = {}
        )
    }
}

@Preview
@Composable
private fun CurrentStateCardPreview(
    @PreviewParameter(CurrentRecordUiProvider::class) record: CurrentRecordUi
) {
    HLTheme {
        CurrentStateCard(record = record)
    }
}

@Preview
@Composable
private fun VersionCardPreview(
    @PreviewParameter(RecordVersionUiProvider::class) version: RecordVersionUi
) {
    HLTheme {
        VersionCard(version = version)
    }
}
