package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Section header component for grouped lists.
 * Used as sticky header in LazyColumn to separate content by date or category.
 *
 * @param text Header text (typically uppercase)
 * @param modifier Optional modifier
 */
@Composable
fun HLSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(HLColors.Background)
            .padding(
                start = HLSpacing.xl,
                end = HLSpacing.xl,
                top = HLSpacing.lg,
                bottom = HLSpacing.sm
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(HLShapes.small)
                .background(HLColors.SurfaceVariant)
                .padding(horizontal = HLSpacing.xl, vertical = HLSpacing.md)
        ) {
            Text(
                text = text,
                style = HLTypography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                ),
                color = HLColors.OnSurfaceVariant
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun SectionHeaderPreview() {
    HLTheme {
        Column(Modifier.background(HLColors.Background)) {
            HLSectionHeader(text = "28 АПРЕЛЯ 2026")
            HLSectionHeader(text = "ПОНЕДЕЛЬНИК")
            HLSectionHeader(text = "СЕГОДНЯ")
        }
    }
}
