package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
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
    val hitchLogRecord by viewModel.record
    val date by rememberSaveable { viewModel.date }
    val time by rememberSaveable { viewModel.time }

    Column {
        Text(text = stringResource(hitchLogRecord.type.text))

        TextField(
            value = date,
            onValueChange = { viewModel.updateDate(it) },
            label = { Text(stringResource(Res.string.date))},
            singleLine = true
        )
        TextField(
            value = time,
            onValueChange = { viewModel.updateTime(it) },
            label = { Text(stringResource(Res.string.time))},
            singleLine = true
        )
        TextField(
            value = hitchLogRecord.text,
            onValueChange = { viewModel.updateText(it) },
            label = { Text(stringResource(Res.string.text))}
        )

        Row {
            Button(onClick = {
                viewModel.save()
                finish()
            }) {
                Text(stringResource(Res.string.ok))
            }

            if (hitchLogRecord.id.isNotEmpty()) {
                Button(onClick = {
                    viewModel.delete()
                    finish()
                }) {
                    Text(stringResource(Res.string.delete))
                }
            }
        }
    }
}