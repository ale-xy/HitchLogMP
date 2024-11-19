package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogState
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.ViewState
import org.jetbrains.compose.resources.stringResource


@Composable
fun HitchLogScreen(
    viewModel: HitchLogViewModel,
    createRecord: (type: HitchLogRecordType) -> Unit,
    editRecord: (id: String) -> Unit
) {
    val state: ViewState<HitchLogState> by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is ViewState.Loading -> Loading()
        is ViewState.Error -> Error((state as ViewState.Error).error)
        is ViewState.Show -> HitchLog(
            log = (state as ViewState.Show<HitchLogState>).value.log,
            records = (state as ViewState.Show<HitchLogState>).value.records,
            createRecord = createRecord,
            editRecord = editRecord
        )
    }
}

@OptIn(FormatStringsInDatetimeFormats::class)
@Composable
fun HitchLog(
    log: HitchLog,
    records: List<HitchLogRecord>,
    createRecord: (type: HitchLogRecordType) -> Unit,
    editRecord: (id: String) -> Unit
) {
    val timeFormat = LocalDateTime.Format {  byUnicodePattern("HH:mm") }

    Column(Modifier.fillMaxSize()) {
        Text(text = log.name)


        LazyColumn(modifier = Modifier.weight(1f)) {
            items(records) { item ->
                item.time.format(timeFormat)
                Row (
                    Modifier.clickable { editRecord(item.id) },
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(text = timeFormat.format(item.time))
                    Text(text = stringResource(item.type.text))
                    Text(text = item.text)
                }
            }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(4)) {
            items(HitchLogRecordType.entries) { item ->
                Button(onClick = { createRecord(item) }) {
                    Text(text = stringResource(item.text))
                }
            }
        }
    }
}