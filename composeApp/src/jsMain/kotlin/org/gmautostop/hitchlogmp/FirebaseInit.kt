package org.gmautostop.hitchlogmp

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

fun initializeFirebaseForWeb() {
    Firebase.initialize(
        options = FirebaseOptions(
            apiKey = FirebaseConfig.apiKey,
            authDomain = FirebaseConfig.authDomain,
            projectId = FirebaseConfig.projectId,
            storageBucket = FirebaseConfig.storageBucket,
            gcmSenderId = FirebaseConfig.gcmSenderId,
            applicationId = FirebaseConfig.applicationId
        )
    )
}
