package org.gmautostop.hitchlogmp

/**
 * JS implementation of FirebaseConfig.
 * Values are read from environment variables injected at build time.
 */
@JsExport
actual object FirebaseConfig {
    actual val apiKey: String
        get() = js("process.env.FIREBASE_API_KEY || ''") as String
    
    actual val authDomain: String
        get() = js("process.env.FIREBASE_AUTH_DOMAIN || ''") as String
    
    actual val projectId: String
        get() = js("process.env.FIREBASE_PROJECT_ID || ''") as String
    
    actual val storageBucket: String
        get() = js("process.env.FIREBASE_STORAGE_BUCKET || ''") as String
    
    actual val gcmSenderId: String
        get() = js("process.env.FIREBASE_GCM_SENDER_ID || ''") as String
    
    actual val applicationId: String
        get() = js("process.env.FIREBASE_APPLICATION_ID || ''") as String
}
