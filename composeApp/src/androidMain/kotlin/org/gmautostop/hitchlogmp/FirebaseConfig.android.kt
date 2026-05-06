package org.gmautostop.hitchlogmp

/**
 * Android implementation of FirebaseConfig.
 * 
 * Public values (authDomain, projectId, storageBucket) are from FirebasePublicConfig.
 * Secret values (apiKey, gcmSenderId, applicationId) are injected from BuildConfig
 * (which reads from local.properties).
 */
actual object FirebaseConfig {
    // Secret - from BuildConfig
    actual val apiKey: String = BuildConfig.FIREBASE_API_KEY
    
    // Public - from shared config
    actual val authDomain: String = FirebasePublicConfig.AUTH_DOMAIN
    
    // Public - from shared config
    actual val projectId: String = FirebasePublicConfig.PROJECT_ID
    
    // Public - from shared config
    actual val storageBucket: String = FirebasePublicConfig.STORAGE_BUCKET
    
    // Secret - from BuildConfig
    actual val gcmSenderId: String = BuildConfig.FIREBASE_GCM_SENDER_ID
    
    // Secret - from BuildConfig
    actual val applicationId: String = BuildConfig.FIREBASE_APPLICATION_ID
}
