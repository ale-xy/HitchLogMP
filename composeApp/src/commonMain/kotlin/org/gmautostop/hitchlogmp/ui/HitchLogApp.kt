package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import kotlinx.coroutines.launch
import org.gmautostop.hitchlogmp.data.AuthService
import org.gmautostop.hitchlogmp.data.FirestoreSyncTracker
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.platformWindowInsetsPadding
import org.gmautostop.hitchlogmp.ui.auth.AuthScreen
import org.gmautostop.hitchlogmp.ui.auth.EmailLoginScreen
import org.gmautostop.hitchlogmp.ui.auth.EmailLoginViewModel
import org.gmautostop.hitchlogmp.ui.auth.EmailRegisterScreen
import org.gmautostop.hitchlogmp.ui.auth.EmailRegisterViewModel
import org.gmautostop.hitchlogmp.ui.auth.ForgotPasswordScreen
import org.gmautostop.hitchlogmp.ui.auth.ForgotPasswordSentScreen
import org.gmautostop.hitchlogmp.ui.auth.ForgotPasswordViewModel
import org.gmautostop.hitchlogmp.ui.designsystem.theme.HLTheme
import org.gmautostop.hitchlogmp.ui.hitchlog.HitchLogScreen
import org.gmautostop.hitchlogmp.ui.hitchlog.HitchLogViewModel
import org.gmautostop.hitchlogmp.ui.recordedit.EditRecordScreen
import org.gmautostop.hitchlogmp.ui.recordedit.EditRecordViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun HitchLogApp(navController: NavHostController) {
    val authService = koinInject<AuthService>()
    val startDestination: Screen = if (authService.currentUser.value != null) Screen.LogList else Screen.Auth

    HLTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.platformWindowInsetsPadding()
            ) {
                composable<Screen.Auth> {
                    AuthScreen(
                        onAuthenticated = {
                            navController.navigate(Screen.LogList) {
                                popUpTo(Screen.Auth) { inclusive = true }
                            }
                        },
                        onNavigateToEmailLogin = {
                            navController.navigate(Screen.EmailLogin)
                        }
                    )
                }
                
                composable<Screen.EmailLogin> {
                    EmailLoginScreen(
                        onAuthenticated = {
                            navController.navigate(Screen.LogList) {
                                popUpTo(Screen.Auth) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.navigateUp() },
                        onNavigateToRegister = { navController.navigate(Screen.EmailRegister) },
                        onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword) },
                        viewModel = koinViewModel<EmailLoginViewModel>()
                    )
                }
                
                composable<Screen.EmailRegister> {
                    EmailRegisterScreen(
                        onAuthenticated = {
                            navController.navigate(Screen.LogList) {
                                popUpTo(Screen.Auth) { inclusive = true }
                            }
                        },
                        onNavigateBack = { navController.navigateUp() },
                        onNavigateToLogin = { navController.navigateUp() },
                        viewModel = koinViewModel<EmailRegisterViewModel>()
                    )
                }
                
                composable<Screen.ForgotPassword> {
                    ForgotPasswordScreen(
                        onNavigateBack = { navController.navigateUp() },
                        onEmailSent = { email ->
                            navController.navigate(Screen.ForgotPasswordSent(email))
                        },
                        viewModel = koinViewModel<ForgotPasswordViewModel>()
                    )
                }
                
                composable<Screen.ForgotPasswordSent> { backStackEntry ->
                    val forgotPasswordSent: Screen.ForgotPasswordSent = backStackEntry.toRoute()
                    ForgotPasswordSentScreen(
                        email = forgotPasswordSent.email,
                        onNavigateToAuth = {
                            navController.navigate(Screen.Auth) {
                                popUpTo(Screen.Auth) { inclusive = true }
                            }
                        }
                    )
                }
                
                composable<Screen.LogList> {
                    val authService = koinInject<AuthService>()
                    val syncTracker = koinInject<FirestoreSyncTracker>()
                    val scope = rememberCoroutineScope()
                    
                    LogListScreen(
                        viewModel = koinViewModel<LogListViewModel>(),
                        openLog = { id -> navController.navigate(Screen.Log(id)) },
                        createLog = { navController.navigate(Screen.EditLog()) },
                        editLog = { id -> navController.navigate(Screen.EditLog(id)) },
                        signOut = {
                            scope.launch {
                                authService.signOut()
                                syncTracker.reset()
                                navController.navigate(Screen.Auth) {
                                    popUpTo(Screen.LogList) { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable<Screen.EditLog> { backStackEntry ->
                    val editLog: Screen.EditLog = backStackEntry.toRoute()
                    EditLogScreen(
                        viewModel = koinViewModel<EditLogViewModel> { parametersOf(editLog.logId) },
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable<Screen.Log> { backStackEntry ->
                    val hitchLog: Screen.Log = backStackEntry.toRoute()
                    HitchLogScreen(
                        viewModel = koinViewModel<HitchLogViewModel> { parametersOf(hitchLog.logId) },
                        navigateUp = { navController.navigateUp() },
                        editLog = { logId -> navController.navigate(Screen.EditLog(logId)) },
                        createRecord = { type ->
                            navController.navigate(Screen.EditRecord(logId = hitchLog.logId, recordType = type.name))
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
                            parametersOf(
                                editRecord.logId,
                                editRecord.recordId,
                                HitchLogRecordType.valueOf(editRecord.recordType)
                            )
                        },
                        finish = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
