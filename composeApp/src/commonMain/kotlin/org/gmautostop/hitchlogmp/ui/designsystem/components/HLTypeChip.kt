package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.chipColorsForRole
import org.gmautostop.hitchlogmp.ui.toStringResource
import org.gmautostop.hitchlogmp.ui.toUi
import org.jetbrains.compose.resources.stringResource

/**
 * Read-only type indicator chip for EditRecordScreen.
 * Displays record type icon and label in a horizontal layout.
 *
 * @param type The record type to display
 * @param modifier Optional modifier
 */
@Composable
fun HLTypeChip(
    type: HitchLogRecordType,
    modifier: Modifier = Modifier
) {
    val recordTypeUi = type.toUi()
    val chipColors = chipColorsForRole(recordTypeUi.colorRole)
    val label = stringResource(type.toStringResource())

    Row(
        modifier = modifier
            .clip(HLShapes.pill)
            .background(HLColors.SurfaceVariant)
            .padding(start = 4.dp, end = 14.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HLIconBadge(
            icon = recordTypeUi.icon,
            chipColors = chipColors,
            size = IconBadgeSize.SMALL
        )
        Text(
            text = label,
            style = HLTypography.labelMedium,
            color = HLColors.OnSurface
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TypeChipPreview() {
    HLTheme {
        Column(
            Modifier
                .background(HLColors.Background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HLTypeChip(type = HitchLogRecordType.LIFT)
            HLTypeChip(type = HitchLogRecordType.CHECKPOINT)
            HLTypeChip(type = HitchLogRecordType.REST_ON)
            HLTypeChip(type = HitchLogRecordType.FINISH)
        }
    }
}
