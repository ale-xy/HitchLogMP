package org.gmautostop.hitchlogmp

/**
 * Android implementation of FirebaseConfig.
 * Values are injected from BuildConfig (which reads from local.properties).
 */
actual object FirebaseConfig {
    actual val apiKey: String = BuildConfig.FIREBASE_API_KEY
    actual val authDomain: String = BuildConfig.FIREBASE_AUTH_DOMAIN
    actual val projectId: String = BuildConfig.FIREBASE_PROJECT_ID
    actual val storageBucket: String = BuildConfig.FIREBASE_STORAGE_BUCKET
    actual val gcmSenderId: String = BuildConfig.FIREBASE_GCM_SENDER_ID
    actual val applicationId: String = BuildConfig.FIREBASE_APPLICATION_ID
}
