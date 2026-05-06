package org.gmautostop.hitchlogmp

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun formatDateLocale(date: LocalDate): String

expect fun shareFile(content: String, mimeType: String, fileName: String)

expect fun shareFileBytes(content: ByteArray, mimeType: String, fileName: String)

expect suspend fun awaitFirestorePendingWrites()

expect fun isGoogleAuthUiSupported(): Boolean

/**
 * Returns the app version string for display.
 * Format: "v{versionName} ({versionCode})" on Android, null on other platforms.
 * Example: "v0.2.0 (200)"
 */
expect fun getAppVersion(): String?

/**
 * Applies platform-specific window insets padding.
 * On Android/iOS: applies safeDrawing insets for system bars.
 * On Web: no-op (web doesn't need window insets).
 */
@Composable
expect fun androidx.compose.ui.Modifier.platformWindowInsetsPadding(): androidx.compose.ui.Modifier