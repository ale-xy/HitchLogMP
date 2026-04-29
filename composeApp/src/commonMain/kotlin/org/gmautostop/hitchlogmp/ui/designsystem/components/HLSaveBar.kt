package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLShapes
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLTypography

/**
 * Bottom save bar with full-width primary button.
 * Used in EditRecordScreen for saving changes.
 *
 * @param label Button label text
 * @param enabled Whether the button is enabled
 * @param onClick Click handler
 * @param modifier Optional modifier
 */
@Composable
fun HLSaveBar(
    label: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(HLColors.Surface)
    ) {
        HorizontalDivider(
            color = HLColors.OutlineVariant,
            thickness = 1.dp
        )
        
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = HLColors.Primary,
                contentColor = HLColors.OnPrimary,
                disabledContainerColor = HLColors.SurfaceVariant,
                disabledContentColor = HLColors.OnSurfaceVariant
            ),
            shape = HLShapes.medium
        ) {
            Text(
                text = label,
                style = HLTypography.labelLarge
            )
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun SaveBarEnabledPreview() {
    HLTheme {
        Box(Modifier.background(HLColors.Background)) {
            HLSaveBar(
                label = "Сохранить",
                enabled = true,
                onClick = { }
            )
        }
    }
}

@Preview
@Composable
private fun SaveBarDisabledPreview() {
    HLTheme {
        Box(Modifier.background(HLColors.Background)) {
            HLSaveBar(
                label = "Сохранить",
                enabled = false,
                onClick = { }
            )
        }
    }
}
