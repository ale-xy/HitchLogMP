package org.gmautostop.hitchlogmp.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.edit_chronicle
import org.gmautostop.hitchlogmp.ui.designsystem.components.HLIconBadge
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.ColorRole
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogUi
import org.jetbrains.compose.resources.stringResource

/**
 * List item card for displaying a chronicle in LogListScreen.
 *
 * @param chronicle Chronicle data to display
 * @param onOpen Called when the card body is clicked
 * @param onEdit Called when the edit button is clicked
 * @param modifier Optional modifier
 */
@Composable
fun ChronicleCard(
    chronicle: HitchLogUi,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = HLColors.OutlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onOpen)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon badge
        HLIconBadge(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            colorRole = ColorRole.PRIMARY,
            size = org.gmautostop.hitchlogmp.ui.designsystem.components.IconBadgeSize.LARGE
        )

        // Text content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Chronicle name
            Text(
                text = chronicle.name,
                style = HLTypography.titleMedium.copy(fontWeight = FontWeight.Medium),
                color = HLColors.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Edit button
        IconButton(
            onClick = onEdit,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(Res.string.edit_chronicle),
                tint = HLColors.OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ChronicleCardShortNamePreview() {
    HLTheme {
        Column(Modifier.padding(16.dp)) {
            ChronicleCard(
                chronicle = HitchLogUi(
                    id = "1",
                    name = "Москва → СПб",
                    formattedDate = "5.05.2026"
                ),
                onOpen = { },
                onEdit = { }
            )
        }
    }
}

@Preview
@Composable
private fun ChronicleCardLongNamePreview() {
    HLTheme {
        Column(Modifier.padding(16.dp)) {
            ChronicleCard(
                chronicle = HitchLogUi(
                    id = "2",
                    name = "Москва → Санкт-Петербург → Петрозаводск → Мурманск",
                    formattedDate = "15.04.2026"
                ),
                onOpen = { },
                onEdit = { }
            )
        }
    }
}
