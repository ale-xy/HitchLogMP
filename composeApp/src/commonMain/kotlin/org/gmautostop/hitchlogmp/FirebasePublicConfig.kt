package org.gmautostop.hitchlogmp

/**
 * Public Firebase configuration values.
 * These are safe to commit to git as they only identify the project.
 * 
 * Secret values (apiKey, gcmSenderId, applicationId) are stored separately:
 * - Local dev: local.properties
 * - CI/CD: GitHub Secrets
 */
object FirebasePublicConfig {
    const val AUTH_DOMAIN = "hitchlogmp.firebaseapp.com"
    const val PROJECT_ID = "hitchlogmp"
    const val STORAGE_BUCKET = "hitchlogmp.firebasestorage.app"
}
