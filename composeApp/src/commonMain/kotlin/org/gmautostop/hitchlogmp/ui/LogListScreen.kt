package org.gmautostop.hitchlogmp.ui

import androidx.compose.material.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.create
import hitchlogmp.composeapp.generated.resources.my_logs
import hitchlogmp.composeapp.generated.resources.no_logs
import org.gmautostop.hitchlogmp.data.HitchLog
import org.gmautostop.hitchlogmp.ui.viewmodel.LogListViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.ViewState
import org.jetbrains.compose.resources.stringResource


@Composable
fun LogListScreen(
    viewModel: LogListViewModel,
    openLog: (id: String) -> Unit,
    createLog: () -> Unit,
    editLog: (id: String) -> Unit) {

    val state: ViewState<List<HitchLog>> by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is ViewState.Loading -> Loading()
        is ViewState.Error -> {
            Error((state as ViewState.Error).error)
        }
        is ViewState.Show<List<HitchLog>> -> LogList(
            list = (state as ViewState.Show<List<HitchLog>>).value,
            openLog,
            createLog,
            editLog
        )
    }
}

@Composable
fun LogList(
    list: List<HitchLog>,
    openLog: (id: String) -> Unit,
    createLog: () -> Unit,
    editLog: (id: String) -> Unit
) {
    Scaffold (
        floatingActionButton = {
            FloatingActionButton(onClick = { createLog() }) {
                Icon(Icons.Filled.Add, stringResource(Res.string.create))
            }
        }) {
        if(list.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(Res.string.no_logs)
                )
            }
        } else {
            Column {
                Text(text = stringResource(Res.string.my_logs))
                LazyColumn {
                    items(list) { item ->
                        Row(Modifier.clickable { openLog(item.id) }) {
                            Text(text = item.name,
                                modifier = Modifier
                                    .padding(vertical = Dp(5.0f))
                            )
                            IconButton(onClick = {
                                editLog(item.id)
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit")
                            }

                        }
                        //todo edit button
                    }
                }
            }
        }
    }
}
