package org.gmautostop.hitchlogmp

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import androidx.navigation.compose.rememberNavController
import kotlinx.browser.document
import org.gmautostop.hitchlogmp.app.Initializer
import org.gmautostop.hitchlogmp.di.initKoin
import org.gmautostop.hitchlogmp.ui.HitchLogApp

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    try {
        // Load js-joda timezone database for kotlinx-datetime
        js("require('@js-joda/timezone')")
        console.log("js-joda timezone database loaded")
        
        initializeFirebaseForWeb()
        Initializer.onApplicationStart()
        initKoin()
        ComposeViewport(document.getElementById("ComposeTarget")!!) {
            val navController = rememberNavController()
            HitchLogApp(navController)
        }
    } catch (e: Throwable) {
        console.error("HitchLogMP: Initialization failed", e)
        throw e
    }
}