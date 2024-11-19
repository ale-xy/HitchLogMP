package org.gmautostop.hitchlogmp

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.gmautostop.hitchlogmp.ui.HitchLogApp

class MainActivity : ComponentActivity() {
    private var navController: NavHostController? = null
    private val navState = "navState"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            navController.restoreState(savedInstanceState?.getBundle(navState))
            HitchLogApp(navController)
        }
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        navController?.let { outState.putBundle(navState, it.saveState()) }
    }
}
