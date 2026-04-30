package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.ui.hitchlog.HitchLogScreen
import org.gmautostop.hitchlogmp.ui.hitchlog.HitchLogViewModel
import org.gmautostop.hitchlogmp.ui.recordedit.EditRecordScreen
import org.gmautostop.hitchlogmp.ui.recordedit.EditRecordViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.AuthViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.EditLogViewModel
import org.gmautostop.hitchlogmp.ui.viewmodel.LogListViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HitchLogApp(navController: NavHostController) {
    val authService = koinInject<AuthService>()
    val startDestination: Screen = if (authService.currentUser.value != null) Screen.LogList else Screen.Auth

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                composable<Screen.Auth> {
                    AuthScreen(koinViewModel<AuthViewModel>()) {
                        navController.navigate(Screen.LogList) {
                            popUpTo(Screen.Auth) { inclusive = true }
                        }
                    }
                }
                composable<Screen.LogList> {
                    val authViewModel = koinViewModel<AuthViewModel>()
                    LogListScreen(
                        koinViewModel<LogListViewModel>(),
                        openLog = { id -> navController.navigate(Screen.Log(id)) },
                        createLog = { navController.navigate(Screen.EditLog()) },
                        editLog = { id -> navController.navigate(Screen.EditLog(id)) },
                        signOut = {
                            authViewModel.onSignOut()
                            navController.navigate(Screen.Auth) {
                                popUpTo(Screen.LogList) { inclusive = true }
                            }
                        }
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
                        viewModel = koinViewModel<HitchLogViewModel> { parametersOf(hitchLog.logId) },
                        navigateUp = { navController.navigateUp() },
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
                        koinViewModel<EditRecordViewModel> {
                            parametersOf(editRecord.logId, editRecord.recordId, editRecord.recordType)
                        },
                        finish = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
