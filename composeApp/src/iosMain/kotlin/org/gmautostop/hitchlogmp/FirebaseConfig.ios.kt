package org.gmautostop.hitchlogmp

/**
 * iOS implementation of FirebaseConfig.
 * Values are read from GoogleService-Info.plist at runtime.
 * For now, using placeholder values - iOS typically uses the plist file directly.
 */
actual object FirebaseConfig {
    actual val apiKey: String = ""
    actual val authDomain: String = ""
    actual val projectId: String = ""
    actual val storageBucket: String = ""
    actual val gcmSenderId: String = ""
    actual val applicationId: String = ""
}
