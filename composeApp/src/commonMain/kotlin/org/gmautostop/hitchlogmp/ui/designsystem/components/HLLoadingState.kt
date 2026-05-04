package org.gmautostop.hitchlogmp.ui.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors

/**
 * Standard loading state component for full-screen loading indicators.
 * 
 * Displays a centered circular progress indicator with the primary color
 * on a background surface.
 * 
 * @param modifier Optional modifier for the container
 */
@Composable
fun HLLoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HLColors.Background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = HLColors.Primary
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun HLLoadingStatePreview() {
    HLTheme {
        HLLoadingState()
    }
}
