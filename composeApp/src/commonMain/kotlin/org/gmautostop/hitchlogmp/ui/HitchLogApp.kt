package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.gmautostop.hitchlogmp.ui.viewmodel.AuthViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.EditLogViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.HitchLogViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.LogListViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.RecordViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
@Preview
fun HitchLogApp(navController: NavHostController) {
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            NavHost(navController = navController, startDestination = Screen.Auth) {
                composable<Screen.Auth> {
                    AuthScreen(koinViewModel<AuthViewModel>()) {
                        navController.navigate(Screen.LogList)
                    }
                }
                composable<Screen.LogList> {
                    LogListScreen(
                        koinViewModel<LogListViewModel>(),
                        openLog = { id -> navController.navigate(Screen.Log(id)) },
                        createLog = { navController.navigate(Screen.EditLog()) },
                        editLog = { id -> navController.navigate(Screen.EditLog(id)) }
                    )
                }
                composable<Screen.EditLog> { backStackEntry ->
                    val editLog: Screen.EditLog = backStackEntry.toRoute()
                    EditLogScreen(
                        koinViewModel<EditLogViewModel> { parametersOf(editLog.logId) },
                        finish = { navController.popBackStack() }
                    )
                }
                composable<Screen.Log> { backStackEntry ->
                    val hitchLog: Screen.Log = backStackEntry.toRoute()
                    HitchLogScreen(
                        koinViewModel<HitchLogViewModel> { parametersOf(hitchLog.logId) },
                        createRecord = { type ->
                            navController.navigate(Screen.EditRecord(logId = hitchLog.logId, recordType = type))
                        },
                        editRecord = { id ->
                            navController.navigate(Screen.EditRecord(logId = hitchLog.logId, recordId = id))
                        }
                    )
                }
                composable<Screen.EditRecord> { backStackEntry ->
                    val editRecord: Screen.EditRecord = backStackEntry.toRoute()
                    EditRecordScreen(
                        koinViewModel<RecordViewModel> {
                            parametersOf(editRecord.logId, editRecord.recordId, editRecord.recordType)
                        },
                        finish = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}