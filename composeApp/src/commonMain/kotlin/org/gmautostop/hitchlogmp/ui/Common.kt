package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.flow.Flow
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors

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

/**
 * Observes a flow of events and executes the given action for each event.
 * Used for one-time side effects like navigation or showing snackbars.
 */
@Composable
fun <T> ObserveAsEvents(
    events: Flow<T>,
    onEvent: (T) -> Unit
) {
    LaunchedEffect(Unit) {
        events.collect { event ->
            onEvent(event)
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun ErrorPreview() {
    HLTheme {
        Box(Modifier.fillMaxSize().background(HLColors.Background)) {
            Error("Не удалось загрузить данные")
        }
    }
}
