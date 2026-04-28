package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.delete
import hitchlogmp.composeapp.generated.resources.name
import hitchlogmp.composeapp.generated.resources.ok
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.HLColors
import org.gmautostop.hitchlogmp.ui.preview.EditLogStateProvider
import org.gmautostop.hitchlogmp.ui.viewmodel.EditLogViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.ViewState
import org.jetbrains.compose.resources.stringResource


@Composable
fun EditLogScreen(
    viewModel: EditLogViewModel,
    finish: () -> Unit
) {
    val state: ViewState<HitchLog> by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { finish() }
    }

    when (state) {
        is ViewState.Loading -> Loading()
        is ViewState.Error -> Error((state as ViewState.Error).error.displayMessage)
        is ViewState.Show<HitchLog> ->
            Log(
                (state as ViewState.Show<HitchLog>).value,
                { name -> viewModel.updateName(name) },
                { viewModel.saveLog() },
                { viewModel.deleteLog() }
            )
    }
}

@Composable
fun Log(
    log: HitchLog,
    updateName: (String) -> Unit,
    saveLog: () -> Unit,
    deleteLog: () -> Unit
) {
    Column {
        TextField(value = log.name,
            onValueChange = { updateName(it) },
            label = { Text(stringResource(Res.string.name))}
        )

        Row {
            Button(onClick = { saveLog() }) {
                Text(stringResource(Res.string.ok))
            }

            if (log.id.isNotEmpty()) {
                Button(onClick = { deleteLog() }) {
                    Text(stringResource(Res.string.delete))
                }
            }
        }
    }
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview
@Composable
private fun EditLogScreenPreview(
    @PreviewParameter(EditLogStateProvider::class) state: ViewState<HitchLog>
) {
    HLTheme {
        Box(
            Modifier
                .fillMaxSize()
                .background(HLColors.Background)
        ) {
            when (state) {
                is ViewState.Loading -> Loading()
                is ViewState.Error -> Error(state.error.displayMessage)
                is ViewState.Show -> Log(
                    log = state.value,
                    updateName = {},
                    saveLog = {},
                    deleteLog = {}
                )
            }
        }
    }
}
