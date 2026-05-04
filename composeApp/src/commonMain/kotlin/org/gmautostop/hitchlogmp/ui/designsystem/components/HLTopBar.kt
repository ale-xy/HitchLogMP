package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Standard top app bar for HitchLog screens.
 * Features animated background color on scroll and optional subtitle.
 *
 * @param title Main title text
 * @param subtitle Optional subtitle text (e.g., team name)
 * @param scrolled Whether the content is scrolled (triggers background animation)
 * @param onNavigateUp Back button click handler
 * @param navigationIcon Icon to display in navigation button (default: back arrow)
 * @param actions Optional trailing actions (e.g., menu button)
 */
@Composable
fun HLTopBar(
    title: String,
    subtitle: String? = null,
    showNavigationButton: Boolean = true,
    onNavigateUp: () -> Unit,
    navigationIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(HLColors.Surface)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showNavigationButton) {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = "Назад",
                        tint = HLColors.OnSurfaceVariant
                    )
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .padding(start = if (showNavigationButton) 4.dp else 16.dp)
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

@Preview
@Composable
private fun TopBarNoNavigationPreview() {
    HLTheme {
        Column {
            HLTopBar(
                title = "Мои хроники",
                showNavigationButton = false,
                onNavigateUp = { },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = HLColors.OnSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}
