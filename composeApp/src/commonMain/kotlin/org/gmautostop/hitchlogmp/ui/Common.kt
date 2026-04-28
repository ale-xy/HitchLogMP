package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors

@Composable
fun Loading() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = HLColors.Primary
        )
    }
}

@Composable
fun Error(error: String) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = error,
            modifier = Modifier.align(Alignment.Center),
            color = HLColors.Error
        )
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun LoadingPreview() {
    HLTheme {
        Box(Modifier.fillMaxSize().background(HLColors.Background)) {
            Loading()
        }
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    HLTheme {
        Box(Modifier.fillMaxSize().background(HLColors.Background)) {
            Error("Не удалось загрузить данные")
        }
    }
}