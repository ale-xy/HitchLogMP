package org.gmautostop.hitchlogmp

/**
 * JS implementation of FirebaseConfig.
 * 
 * Public values (authDomain, projectId, storageBucket) are from FirebasePublicConfig.
 * Secret values (apiKey, gcmSenderId) are injected at build time
 * from local.properties (local dev) or environment variables (CI/CD).
 * 
 * Web uses FIREBASE_WEB_API_KEY (Browser key), separate from Android's FIREBASE_API_KEY.
 */
@JsExport
actual object FirebaseConfig {
    // Secret - injected at build time via webpack DefinePlugin (Browser key for web)
    actual val apiKey: String
        get() = js("process.env.FIREBASE_WEB_API_KEY || ''") as String
    
    // Public - from shared config
    actual val authDomain: String = FirebasePublicConfig.AUTH_DOMAIN
    
    // Public - from shared config
    actual val projectId: String = FirebasePublicConfig.PROJECT_ID
    
    // Public - from shared config
    actual val storageBucket: String = FirebasePublicConfig.STORAGE_BUCKET
    
    // Secret - injected at build time via webpack DefinePlugin
    actual val gcmSenderId: String
        get() = js("process.env.FIREBASE_GCM_SENDER_ID || ''") as String
    
    actual val applicationId: String
        get() = ""
}
