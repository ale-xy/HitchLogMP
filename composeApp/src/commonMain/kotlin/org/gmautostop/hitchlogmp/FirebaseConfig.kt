package org.gmautostop.hitchlogmp

/**
 * Firebase configuration holder.
 * Values are provided by platform-specific implementations.
 */
expect object FirebaseConfig {
    val apiKey: String
    val authDomain: String
    val projectId: String
    val storageBucket: String
    val gcmSenderId: String
    val applicationId: String
}
