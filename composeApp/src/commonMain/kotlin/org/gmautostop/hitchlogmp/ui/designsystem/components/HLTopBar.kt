package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLSpacing
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Standard top app bar for HitchLog screens.
 * Features animated background color on scroll and optional subtitle.
 *
 * @param title Main title text
 * @param subtitle Optional subtitle text (e.g., team name)
 * @param scrolled Whether the content is scrolled (triggers background animation)
 * @param onNavigateUp Back button click handler
 * @param actions Optional trailing actions (e.g., menu button)
 */
@Composable
fun HLTopBar(
    title: String,
    subtitle: String? = null,
    scrolled: Boolean = false,
    onNavigateUp: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val bg by animateColorAsState(
        targetValue = if (scrolled) HLColors.SurfaceContainer else HLColors.Surface,
        animationSpec = tween(200),
        label = "topBarBg"
    )

    Column(
        Modifier
            .fillMaxWidth()
            .background(bg)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = HLSpacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = HLColors.OnSurfaceVariant
                )
            }

            Column(
                Modifier
                    .weight(1f)
                    .padding(start = HLSpacing.xs)
            ) {
                Text(
                    text = title,
                    style = HLTypography.titleLarge,
                    color = HLColors.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = HLTypography.subtitle,
                        color = HLColors.OnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            actions()
        }

        if (scrolled) {
            HorizontalDivider(
                color = HLColors.OutlineVariant,
                thickness = 1.dp
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun TopBarDefaultPreview() {
    HLTheme {
        Column {
            HLTopBar(
                title = "Москва → Санкт-Петербург",
                scrolled = false,
                onNavigateUp = { }
            )
        }
    }
}

@Preview
@Composable
private fun TopBarScrolledPreview() {
    HLTheme {
        Column {
            HLTopBar(
                title = "Москва → Санкт-Петербург",
                scrolled = true,
                onNavigateUp = { }
            )
        }
    }
}

@Preview
@Composable
private fun TopBarWithSubtitlePreview() {
    HLTheme {
        Column {
            HLTopBar(
                title = "Казань → Екатеринбург",
                subtitle = "Команда №5",
                scrolled = false,
                onNavigateUp = { }
            )
        }
    }
}

@Preview
@Composable
private fun TopBarWithActionsPreview() {
    HLTheme {
        Column {
            HLTopBar(
                title = "Москва → Санкт-Петербург",
                scrolled = false,
                onNavigateUp = { },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = null,
                            tint = HLColors.OnSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}
