package org.gmautostop.hitchlogmp

/**
 * iOS implementation of FirebaseConfig.
 * 
 * Public values (authDomain, projectId, storageBucket) are from FirebasePublicConfig.
 * Secret values (apiKey, gcmSenderId, applicationId) are read from GoogleService-Info.plist at runtime.
 * For now, using empty strings for secrets - iOS typically uses the plist file directly.
 */
actual object FirebaseConfig {
    // Secret - from GoogleService-Info.plist (empty for now)
    actual val apiKey: String = ""
    
    // Public - from shared config
    actual val authDomain: String = FirebasePublicConfig.AUTH_DOMAIN
    
    // Public - from shared config
    actual val projectId: String = FirebasePublicConfig.PROJECT_ID
    
    // Public - from shared config
    actual val storageBucket: String = FirebasePublicConfig.STORAGE_BUCKET
    
    // Secret - from GoogleService-Info.plist (empty for now)
    actual val gcmSenderId: String = ""
    
    // Secret - from GoogleService-Info.plist (empty for now)
    actual val applicationId: String = ""
}
