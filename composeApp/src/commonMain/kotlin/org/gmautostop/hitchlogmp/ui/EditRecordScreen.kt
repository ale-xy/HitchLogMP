package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.date
import hitchlogmp.composeapp.generated.resources.delete
import hitchlogmp.composeapp.generated.resources.ok
import hitchlogmp.composeapp.generated.resources.text
import hitchlogmp.composeapp.generated.resources.time
import org.gmautostop.hitchlogmp.ui.viewmodel.RecordViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditRecordScreen(
    viewModel: RecordViewModel,
    finish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { finish() }
    }

    Column {
        Text(text = stringResource(uiState.record.type.toStringResource()))

        TextField(
            value = uiState.dateText,
            onValueChange = { viewModel.updateDate(it) },
            label = { Text(stringResource(Res.string.date)) },
            singleLine = true
        )
        TextField(
            value = uiState.timeText,
            onValueChange = { viewModel.updateTime(it) },
            label = { Text(stringResource(Res.string.time)) },
            singleLine = true
        )
        TextField(
            value = uiState.record.text,
            onValueChange = { viewModel.updateText(it) },
            label = { Text(stringResource(Res.string.text)) }
        )

        uiState.error?.let { Text(it.displayMessage, color = MaterialTheme.colors.error) }

        Row {
            Button(
                onClick = { viewModel.save() },
                enabled = !uiState.isLoading
            ) {
                Text(stringResource(Res.string.ok))
            }

            if (uiState.record.id.isNotEmpty()) {
                Button(
                    onClick = { viewModel.delete() },
                    enabled = !uiState.isLoading
                ) {
                    Text(stringResource(Res.string.delete))
                }
            }
        }
    }
}
