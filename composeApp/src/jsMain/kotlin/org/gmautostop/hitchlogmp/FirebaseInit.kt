package org.gmautostop.hitchlogmp

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

fun initializeFirebaseForWeb() {
    console.log("Firebase config:", js("""({
        apiKey: process.env.FIREBASE_API_KEY,
        authDomain: process.env.FIREBASE_AUTH_DOMAIN,
        projectId: process.env.FIREBASE_PROJECT_ID,
        storageBucket: process.env.FIREBASE_STORAGE_BUCKET,
        gcmSenderId: process.env.FIREBASE_GCM_SENDER_ID,
        applicationId: process.env.FIREBASE_APPLICATION_ID
    })"""))
    
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
