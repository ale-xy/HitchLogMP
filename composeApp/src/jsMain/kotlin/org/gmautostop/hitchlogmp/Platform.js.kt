package org.gmautostop.hitchlogmp

import androidx.compose.runtime.Composable
import kotlinx.browser.window
import kotlinx.datetime.LocalDate
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

class JSPlatform: Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JSPlatform()

actual fun formatDateLocale(date: LocalDate): String {
    // Extract Kotlin properties first, then pass to JS
    val year = date.year
    val month = date.monthNumber - 1  // JS months are 0-indexed
    val day = date.dayOfMonth
    
    // Create JS Date with extracted values
    val jsDate = js("new Date(year, month, day)")
    val formatter = js("new Intl.DateTimeFormat('default', { day: 'numeric', month: 'long', year: 'numeric', weekday: 'long' })")
    return formatter.format(jsDate) as String
}

actual fun shareFile(content: String, mimeType: String, fileName: String) {
    // Use Web Share API with download fallback
    val blob = Blob(arrayOf(content), BlobPropertyBag(type = mimeType))
    
    // Try Web Share API first
    if (js("navigator.share") != null) {
        val file = js("new File([blob], fileName, { type: mimeType })")
        js("navigator.share({ files: [file] })")
            .catch { error: dynamic ->
                console.log("Share failed, falling back to download", error)
                downloadBlob(blob, fileName)
            }
    } else {
        // Fallback to download
        downloadBlob(blob, fileName)
    }
}

actual fun shareFileBytes(content: ByteArray, mimeType: String, fileName: String) {
    val blob = Blob(arrayOf(content), BlobPropertyBag(type = mimeType))
    
    if (js("navigator.share") != null) {
        val file = js("new File([blob], fileName, { type: mimeType })")
        js("navigator.share({ files: [file] })")
            .catch { error: dynamic ->
                console.log("Share failed, falling back to download", error)
                downloadBlob(blob, fileName)
            }
    } else {
        downloadBlob(blob, fileName)
    }
}

private fun downloadBlob(blob: Blob, fileName: String) {
    val url = URL.createObjectURL(blob)
    val a = window.document.createElement("a") as org.w3c.dom.HTMLAnchorElement
    a.href = url
    a.download = fileName
    window.document.body?.appendChild(a)
    a.click()
    window.document.body?.removeChild(a)
    URL.revokeObjectURL(url)
}

actual suspend fun awaitFirestorePendingWrites() {
    // GitLive Firebase SDK handles this automatically on JS
    // Note: waitForPendingWrites() is not available in GitLive SDK
    // The JS SDK automatically handles pending writes
}

actual fun isGoogleAuthUiSupported(): Boolean = false

@Composable
actual fun androidx.compose.ui.Modifier.platformWindowInsetsPadding(): androidx.compose.ui.Modifier {
    // Web doesn't need window insets - return unmodified
    return this
}
