package org.gmautostop.hitchlogmp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.lighthousegames.logging.logging

@Composable
fun Loading() {
    Box(Modifier.fillMaxSize()) {
        CircularProgressIndicator(Modifier.align(Alignment.Center))
    }
}


@Composable
fun Error(error: String) {
    Box(Modifier.fillMaxSize()) {
        Text(error, Modifier.align(Alignment.Center))
    }
}