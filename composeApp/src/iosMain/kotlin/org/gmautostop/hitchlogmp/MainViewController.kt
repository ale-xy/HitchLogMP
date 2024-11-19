package org.gmautostop.hitchlogmp

import androidx.compose.ui.window.ComposeUIViewController
import androidx.navigation.compose.rememberNavController
import org.gmautostop.hitchlogmp.di.initKoin
import org.gmautostop.hitchlogmp.ui.HitchLogApp

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    val navController = rememberNavController()
    HitchLogApp(navController)
}