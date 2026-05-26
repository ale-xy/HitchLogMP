package org.gmautostop.hitchlogmp.export

import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

class JsFileSaver : FileSaver {
    override suspend fun save(fileName: String, bytes: ByteArray, format: ExportFormat) {
        val blob = Blob(arrayOf(bytes), BlobPropertyBag(type = format.mimeType))
        
        // On iOS, use native share sheet; on desktop, direct download
        val isIOS = js("(/iPad|iPhone|iPod/.test(navigator.userAgent))") as Boolean
        if (isIOS && js("navigator.share") != null) {
            val file = js("new File([blob], fileName, { type: format.mimeType })")
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
        val a = window.document.createElement("a") as HTMLAnchorElement
        a.href = url
        a.download = fileName
        a.style.display = "none"
        window.document.body?.appendChild(a)
        a.click()
        window.document.body?.removeChild(a)
        URL.revokeObjectURL(url)
    }
}
