package org.gmautostop.hitchlogmp.export

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class AndroidFileSaver(private val context: Context) : FileSaver {
    override suspend fun save(fileName: String, bytes: ByteArray, format: ExportFormat) {
        // Write to cache directory
        val file = File(context.cacheDir, fileName)
        file.writeBytes(bytes)
        
        // Create content URI via FileProvider
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        // Branch based on format type
        when (format) {
            ExportFormat.Html, ExportFormat.Csv, ExportFormat.Xlsx -> {
                // Offer both VIEW (primary) and SEND (secondary) options
                launchViewAndSendChooser(uri, format.mimeType)
            }
            ExportFormat.Text -> {
                // Only SEND (share) option
                launchSendOnly(uri, format.mimeType)
            }
        }
    }
    
    private fun launchViewAndSendChooser(uri: Uri, mimeType: String) {
        // Primary intent: VIEW (open in app)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Secondary intent: SEND (share)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(viewIntent, null).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(sendIntent))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        context.startActivity(chooser)
    }
    
    private fun launchSendOnly(uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        val chooser = Intent.createChooser(intent, null)
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
