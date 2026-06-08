package org.gmautostop.hitchlogmp.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.change_type_create
import hitchlogmp.composeapp.generated.resources.change_type_delete
import hitchlogmp.composeapp.generated.resources.change_type_update
import hitchlogmp.composeapp.generated.resources.history_record_created
import hitchlogmp.composeapp.generated.resources.history_record_deleted
import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.domain.model.ChangeType
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.model.RecordFields
import org.gmautostop.hitchlogmp.ui.components.toStringResource
import org.gmautostop.hitchlogmp.ui.components.toUi
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.chipColorsForRole
import org.jetbrains.compose.resources.stringResource

@Composable
fun RecordIconChip(
    type: HitchLogRecordType,
    size: Dp = 36.dp,
    dimmed: Boolean = false
) {
    val typeUi = type.toUi()
    val chipColors = chipColorsForRole(typeUi.colorRole)
    val strokeMod = if (chipColors.stroke != null) {
        Modifier.border(1.dp, chipColors.stroke, CircleShape)
    } else {
        Modifier
    }
    Box(
        modifier = Modifier
            .size(size)
            .alpha(if (dimmed) 0.6f else 1f)
            .background(chipColors.bg, CircleShape)
            .then(strokeMod),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = typeUi.icon,
            contentDescription = stringResource(type.toStringResource()),
            tint = chipColors.fg,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

@Composable
fun ChangeTypeChip(changeType: ChangeType, compact: Boolean = false) {
    val bg = when (changeType) {
        ChangeType.CREATE -> MaterialTheme.colorScheme.secondaryContainer
        ChangeType.UPDATE -> MaterialTheme.colorScheme.primaryContainer
        ChangeType.DELETE -> MaterialTheme.colorScheme.errorContainer
    }
    val fg = when (changeType) {
        ChangeType.CREATE -> MaterialTheme.colorScheme.onSecondaryContainer
        ChangeType.UPDATE -> MaterialTheme.colorScheme.onPrimaryContainer
        ChangeType.DELETE -> MaterialTheme.colorScheme.onErrorContainer
    }
    val icon = when (changeType) {
        ChangeType.CREATE -> Icons.Filled.Add
        ChangeType.UPDATE -> Icons.Filled.Edit
        ChangeType.DELETE -> Icons.Filled.Delete
    }
    val labelRes = when (changeType) {
        ChangeType.CREATE -> Res.string.change_type_create
        ChangeType.UPDATE -> Res.string.change_type_update
        ChangeType.DELETE -> Res.string.change_type_delete
    }
    val vPad = if (compact) 2.dp else 4.dp
    val hPadEnd = if (compact) 8.dp else 10.dp

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(100.dp))
            .padding(top = vPad, bottom = vPad, start = 6.dp, end = hPadEnd),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = stringResource(labelRes),
            style = HLTypography.labelSmall,
            color = fg
        )
    }
}

@Composable
fun TypePill(type: HitchLogRecordType, strike: Boolean = false) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(100.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RecordIconChip(type, size = 20.dp)
        Text(
            text = stringResource(type.toStringResource()),
            style = HLTypography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = if (strike) TextDecoration.LineThrough else null
        )
    }
}

@Composable
fun InlineDiff(before: RecordFields?, after: RecordFields?, changeType: ChangeType) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        when (changeType) {
            ChangeType.CREATE -> after?.let { field ->
                Text(
                    text = field.formattedTime,
                    style = HLTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface)
                field.text?.let {
                    Text(
                        text = it,
                        style = HLTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface)
                }
            }
            ChangeType.DELETE ->
                Text(
                    text = stringResource(Res.string.history_record_deleted),
                    style = HLTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            ChangeType.UPDATE -> {
                val timeChanged = before?.time != after?.time
                val typeChanged = before?.type != after?.type
                val textChanged = before?.text != after?.text
                if (timeChanged) {
                    before?.let {
                        Text(
                            text = it.formattedTime,
                            style = HLTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    after?.let {
                        Text(
                            text = it.formattedTime,
                            style = HLTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (typeChanged) {
                    before?.let { TypePill(it.type, strike = true) }
                    after?.let { TypePill(it.type) }
                }
                if (textChanged) {
                    before?.text?.let {
                        Text(
                            text = it,
                            style = HLTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    after?.text?.let {
                        Text(
                            text = it,
                            style = HLTypography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (!timeChanged && !typeChanged && !textChanged) {
                    Text(
                        text = stringResource(Res.string.history_record_created),
                        style = HLTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ChangeTypeChipsPreview() {
    HLTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ChangeTypeChip(ChangeType.CREATE)
            ChangeTypeChip(ChangeType.UPDATE)
            ChangeTypeChip(ChangeType.DELETE)
        }
    }
}

@Preview
@Composable
private fun TypePillsPreview() {
    HLTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypePill(HitchLogRecordType.LIFT)
            TypePill(HitchLogRecordType.CHECKPOINT, strike = true)
        }
    }
}

@Preview
@Composable
private fun InlineDiffPreview(
    @PreviewParameter(RecordFieldsWithTextProvider::class) fields: RecordFields
) {
    HLTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InlineDiff(before = null, after = fields, changeType = ChangeType.CREATE)
            InlineDiff(
                before = RecordFields(
                    time = LocalDateTime(2025, 5, 19, 10, 0, 0),
                    formattedTime = "10:00",
                    type = HitchLogRecordType.WALK,
                    text = null
                ),
                after = fields,
                changeType = ChangeType.UPDATE
            )
            InlineDiff(before = fields, after = null, changeType = ChangeType.DELETE)
        }
    }
}
