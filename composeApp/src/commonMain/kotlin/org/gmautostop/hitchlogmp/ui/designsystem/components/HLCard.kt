package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Standard card container for HitchLog application.
 * Features rounded corners, border, and consistent padding.
 *
 * @param modifier Optional modifier
 * @param content Card content
 */
@Composable
fun HLCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(HLShapes.medium)
            .background(HLColors.Surface)
            .border(1.dp, HLColors.OutlineVariant, HLShapes.medium)
            .padding(HLSpacing.xl),
        content = content
    )
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun CardPreview() {
    HLTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(HLColors.Background)
                .padding(16.dp)
        ) {
            HLCard {
                Text(
                    text = "Москва → Санкт-Петербург",
                    style = HLTypography.titleMedium,
                    color = HLColors.OnSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Гонка началась 28 апреля 2026 в 10:00",
                    style = HLTypography.bodyMedium,
                    color = HLColors.OnSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Подъёмов", style = HLTypography.labelSmall, color = HLColors.OnSurfaceVariant)
                        Text("5", style = HLTypography.titleLarge, color = HLColors.OnSurface)
                    }
                    Column {
                        Text("КП", style = HLTypography.labelSmall, color = HLColors.OnSurfaceVariant)
                        Text("2", style = HLTypography.titleLarge, color = HLColors.OnSurface)
                    }
                    Column {
                        Text("Отдых", style = HLTypography.labelSmall, color = HLColors.OnSurfaceVariant)
                        Text("02:30", style = HLTypography.titleLarge, color = HLColors.OnSurface)
                    }
                }
            }
        }
    }
}
