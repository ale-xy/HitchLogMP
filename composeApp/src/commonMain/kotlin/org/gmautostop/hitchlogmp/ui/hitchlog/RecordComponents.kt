package org.gmautostop.hitchlogmp.ui.hitchlog

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.formatDateLocale
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLCard
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLIconBadge
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLSectionHeader
import org.gmautostop.hitchlogmp.ui.designsystem.components.IconBadgeSize
import org.gmautostop.hitchlogmp.ui.designsystem.preview.sampleRecord
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.chipColorsForRole
import org.gmautostop.hitchlogmp.ui.toStringResource
import org.gmautostop.hitchlogmp.ui.toUi
import org.jetbrains.compose.resources.stringResource

@Composable
fun DateHeader(date: LocalDate) {
    HLSectionHeader(text = formatDateLocale(date).uppercase())
}

@Composable
fun RecordGroupCard(
    items: List<HitchLogRecord>,
    editRecord: (String) -> Unit,
) {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(HLColors.Surface)
            .border(1.dp, HLColors.OutlineVariant, RoundedCornerShape(12.dp))
    ) {
        items.forEachIndexed { idx, record ->
            RecordItem(
                record = record,
                isLast = idx == items.lastIndex,
                onClick = { editRecord(record.id) },
            )
        }
    }
}

@Composable
internal fun RecordItem(
    record: HitchLogRecord,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val recordTypeUi = record.type.toUi()
    val chipColors = chipColorsForRole(recordTypeUi.colorRole)
    val label = stringResource(record.type.toStringResource())

    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HLIconBadge(
                icon = recordTypeUi.icon,
                chipColors = chipColors,
                size = IconBadgeSize.LARGE
            )

            Column(Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = HLTypography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = HLColors.OnSurface,
                )
                if (record.text.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = record.text,
                        style = HLTypography.bodyMedium,
                        color = HLColors.OnSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Text(
                text = timeFormatForDisplay.format(record.time),
                style = HLTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
                color = HLColors.OnSurfaceVariant,
            )
        }

        if (!isLast) {
            HorizontalDivider(
                color = HLColors.OutlineVariant,
                thickness = 1.dp,
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun DateHeaderPreview() {
    HLTheme {
        Column(Modifier.background(HLColors.Background)) {
            DateHeader(date = LocalDate(2026, 4, 28))
        }
    }
}

@Preview
@Composable
private fun RecordItemVariantsPreview() {
    HLTheme {
        Column(
            Modifier
                .background(HLColors.Background)
                .padding(16.dp)
        ) {
            HLCard {
                RecordItem(
                    record = sampleRecord(
                        type = HitchLogRecordType.START,
                        text = "Старт от метро Сокол"
                    ),
                    isLast = false,
                    onClick = { }
                )
                RecordItem(
                    record = sampleRecord(
                        type = HitchLogRecordType.LIFT,
                        text = "Газель, водитель Владимир",
                        offsetMinutes = 15
                    ),
                    isLast = false,
                    onClick = { }
                )
                RecordItem(
                    record = sampleRecord(
                        type = HitchLogRecordType.CHECKPOINT,
                        text = "КП1 - Владимир, получена отметка",
                        offsetMinutes = 180
                    ),
                    isLast = false,
                    onClick = { }
                )
                RecordItem(
                    record = sampleRecord(
                        type = HitchLogRecordType.GET_OFF,
                        text = "",
                        offsetMinutes = 185
                    ),
                    isLast = false,
                    onClick = { }
                )
                RecordItem(
                    record = sampleRecord(
                        type = HitchLogRecordType.REST_ON,
                        text = "Отдых у заправки",
                        offsetMinutes = 425
                    ),
                    isLast = true,
                    onClick = { }
                )
            }
        }
    }
}

@Preview
@Composable
private fun RecordGroupCardPreview() {
    HLTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(HLColors.Background)
        ) {
            RecordGroupCard(
                items = listOf(
                    sampleRecord(
                        id = "r1",
                        type = HitchLogRecordType.START,
                        text = "Старт от метро Сокол",
                        offsetMinutes = 0
                    ),
                    sampleRecord(
                        id = "r2",
                        type = HitchLogRecordType.LIFT,
                        text = "Газель, Владимир",
                        offsetMinutes = 15
                    ),
                    sampleRecord(
                        id = "r3",
                        type = HitchLogRecordType.CHECKPOINT,
                        text = "КП1 - Владимир",
                        offsetMinutes = 180
                    ),
                    sampleRecord(
                        id = "r4",
                        type = HitchLogRecordType.GET_OFF,
                        text = "Трасса М7",
                        offsetMinutes = 185
                    )
                ),
                editRecord = { }
            )
        }
    }
}
