package org.gmautostop.hitchlogmp.app

import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider

object Initializer {
    fun onApplicationStart() {
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = "869765129540-5pmuegm08d0e77b2idbqqpfkjf7ghpuk.apps.googleusercontent.com"
            )
        )
    }
}