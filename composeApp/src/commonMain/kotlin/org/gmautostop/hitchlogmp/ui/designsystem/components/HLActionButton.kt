package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.chipColorsForRole
import org.gmautostop.hitchlogmp.ui.toStringResource
import org.gmautostop.hitchlogmp.ui.toUi
import org.jetbrains.compose.resources.stringResource

/**
 * Size variants for action buttons.
 */
enum class ActionButtonSize {
    /** Large button with icon and label, used for primary actions */
    BIG,
    /** Medium button with icon and label, used for secondary actions */
    MEDIUM,
    /** Grid tile button used in bottom sheet */
    SHEET
}

/**
 * Action button for creating new records.
 * Displays record type icon and label with appropriate styling.
 *
 * @param type The record type this button represents
 * @param size Size variant (BIG, MEDIUM, SHEET)
 * @param highlight Whether to highlight this button (primary action)
 * @param onClick Click handler
 * @param modifier Optional modifier
 */
@Composable
fun HLActionButton(
    type: HitchLogRecordType,
    size: ActionButtonSize = ActionButtonSize.BIG,
    highlight: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (size) {
        ActionButtonSize.BIG -> BigActionButton(
            type = type,
            highlight = highlight,
            onClick = onClick,
            modifier = modifier
        )
        ActionButtonSize.MEDIUM -> MediumActionButton(
            type = type,
            onClick = onClick,
            modifier = modifier
        )
        ActionButtonSize.SHEET -> SheetActionButton(
            type = type,
            onClick = onClick,
            modifier = modifier
        )
    }
}

@Composable
private fun BigActionButton(
    type: HitchLogRecordType,
    highlight: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordTypeUi = type.toUi()
    val chipColors = chipColorsForRole(recordTypeUi.colorRole)
    val label = stringResource(type.toStringResource())
    val background = if (highlight) HLColors.PrimaryContainer else HLColors.SurfaceVariant
    val foreground = if (highlight) HLColors.OnPrimaryContainer else HLColors.OnSurface

    Row(
        modifier = modifier
            .heightIn(min = 64.dp)
            .clip(HLShapes.medium)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HLIconBadge(
            icon = recordTypeUi.icon,
            chipColors = chipColors,
            size = IconBadgeSize.MEDIUM
        )
        Text(
            text = label,
            style = HLTypography.labelLarge,
            color = foreground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MediumActionButton(
    type: HitchLogRecordType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordTypeUi = type.toUi()
    val chipColors = chipColorsForRole(recordTypeUi.colorRole)
    val label = stringResource(type.toStringResource())

    Row(
        modifier = modifier
            .height(44.dp)
            .clip(HLShapes.medium)
            .border(1.dp, HLColors.OutlineVariant, HLShapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HLSpacing.sm)
    ) {
        HLIconBadge(
            icon = recordTypeUi.icon,
            chipColors = chipColors,
            size = IconBadgeSize.SMALL
        )
        Text(
            text = label,
            style = HLTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = HLColors.OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SheetActionButton(
    type: HitchLogRecordType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recordTypeUi = type.toUi()
    val chipColors = chipColorsForRole(recordTypeUi.colorRole)
    val label = stringResource(type.toStringResource())

    Column(
        modifier = modifier
            .height(92.dp)
            .clip(HLShapes.large)
            .background(HLColors.Surface)
            .border(1.dp, HLColors.OutlineVariant, HLShapes.large)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = HLSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HLIconBadge(
            icon = recordTypeUi.icon,
            chipColors = chipColors,
            size = IconBadgeSize.LARGE
        )
        Spacer(Modifier.height(HLSpacing.sm))
        Text(
            text = label,
            style = HLTypography.labelMedium.copy(fontWeight = FontWeight.Medium),
            color = HLColors.OnSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ActionButtonSizesPreview() {
    HLTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HLActionButton(
                type = HitchLogRecordType.LIFT,
                size = ActionButtonSize.BIG,
                onClick = { }
            )
            HLActionButton(
                type = HitchLogRecordType.LIFT,
                size = ActionButtonSize.MEDIUM,
                onClick = { }
            )
            HLActionButton(
                type = HitchLogRecordType.LIFT,
                size = ActionButtonSize.SHEET,
                onClick = { },
                modifier = Modifier.width(120.dp)
            )
        }
    }
}

@Preview
@Composable
private fun ActionButtonHighlightPreview() {
    HLTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HLActionButton(
                type = HitchLogRecordType.CHECKPOINT,
                size = ActionButtonSize.BIG,
                highlight = false,
                onClick = { }
            )
            HLActionButton(
                type = HitchLogRecordType.CHECKPOINT,
                size = ActionButtonSize.BIG,
                highlight = true,
                onClick = { }
            )
        }
    }
}

@Preview
@Composable
private fun ActionButtonTypesPreview() {
    HLTheme {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HLActionButton(
                type = HitchLogRecordType.START,
                size = ActionButtonSize.MEDIUM,
                onClick = { }
            )
            HLActionButton(
                type = HitchLogRecordType.LIFT,
                size = ActionButtonSize.MEDIUM,
                onClick = { }
            )
            HLActionButton(
                type = HitchLogRecordType.CHECKPOINT,
                size = ActionButtonSize.MEDIUM,
                onClick = { }
            )
            HLActionButton(
                type = HitchLogRecordType.REST_ON,
                size = ActionButtonSize.MEDIUM,
                onClick = { }
            )
            HLActionButton(
                type = HitchLogRecordType.FINISH,
                size = ActionButtonSize.MEDIUM,
                onClick = { }
            )
        }
    }
}
