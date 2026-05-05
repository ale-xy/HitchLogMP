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
 * Applies platform-specific window insets padding.
 * On Android/iOS: applies safeDrawing insets for system bars.
 * On Web: no-op (web doesn't need window insets).
 */
@Composable
expect fun androidx.compose.ui.Modifier.platformWindowInsetsPadding(): androidx.compose.ui.Modifier