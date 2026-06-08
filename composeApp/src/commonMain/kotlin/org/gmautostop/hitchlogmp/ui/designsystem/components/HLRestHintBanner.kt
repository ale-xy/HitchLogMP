package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.timeFormatForDisplay
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.theme.LocalHLExtendedColors

/**
 * Amber banner showing REST_ON start time and elapsed duration.
 * Only displayed when editing a REST_OFF record.
 *
 * @param restOnTime The time when REST_ON was recorded
 * @param elapsedMinutes Minutes elapsed since REST_ON
 * @param modifier Optional modifier
 */
@Composable
fun HLRestHintBanner(
    restOnTime: LocalDateTime,
    elapsedMinutes: Int,
    modifier: Modifier = Modifier
) {
    val formattedTime = timeFormatForDisplay.format(restOnTime)
    val extendedColors = LocalHLExtendedColors.current
    
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(extendedColors.restBannerBg)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = extendedColors.restBannerText,
                modifier = Modifier.size(18.dp)
            )
            
            Text(
                text = buildAnnotatedString {
                    append("Rest начат в ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(formattedTime)
                    }
                    append(" · продолжается ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append("$elapsedMinutes мин")
                    }
                },
                fontSize = 12.sp,
                color = extendedColors.restBannerText
            )
        }
        HorizontalDivider(color = extendedColors.restBannerBorder, thickness = 1.dp)
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun RestHintBannerPreview() {
    HLTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            HLRestHintBanner(
                restOnTime = LocalDateTime(2026, 4, 29, 12, 20),
                elapsedMinutes = 34
            )
        }
    }
}

@Preview
@Composable
private fun RestHintBannerLongDurationPreview() {
    HLTheme {
        Column(Modifier.background(MaterialTheme.colorScheme.background)) {
            HLRestHintBanner(
                restOnTime = LocalDateTime(2026, 4, 29, 8, 15),
                elapsedMinutes = 185
            )
        }
    }
}
