package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.delete
import hitchlogmp.composeapp.generated.resources.name
import hitchlogmp.composeapp.generated.resources.ok
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.ui.viewmodel.EditLogViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.ViewState
import org.jetbrains.compose.resources.stringResource


@Composable
fun EditLogScreen(
    viewModel: EditLogViewModel,
    finish: () -> Unit
) {
    val state: ViewState<HitchLog> by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is ViewState.Loading -> Loading()
        is ViewState.Error -> Error((state as ViewState.Error).error)
        is ViewState.Show<HitchLog> ->
            Log(
                (state as ViewState.Show<HitchLog>).value,
                { name -> viewModel.updateName(name) },
                { viewModel.saveLog() },
                { viewModel.deleteLog() },
                finish
            )
    }
}

@Composable
fun Log(
    log: HitchLog,
    updateName: (String) -> Unit,
    saveLog: () -> Unit,
    deleteLog: () -> Unit,
    finish: () -> Unit
) {
    Column {
        TextField(value = log.name,
            onValueChange = { updateName(it) },
            label = { Text(stringResource(Res.string.name))}
        )

        Row {
            Button(onClick = {
                saveLog()
                finish()
            }) {
                Text(stringResource(Res.string.ok))
            }

            if (log.id.isNotEmpty()) {
                Button(onClick = {
                    deleteLog()
                    finish()
                }) {
                    Text(stringResource(Res.string.delete))
                }
            }
        }
    }
}
