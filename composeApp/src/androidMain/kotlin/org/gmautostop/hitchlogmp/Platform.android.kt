package org.gmautostop.hitchlogmp

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await
import kotlinx.datetime.LocalDate
import org.gmautostop.hitchlogmp.domain.MimeTypes
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun formatDateLocale(date: LocalDate): String {
    val cal = Calendar.getInstance().also {
        it.set(date.year, date.month.ordinal, date.dayOfMonth)
    }
    return SimpleDateFormat("d MMMM yyyy, EEEE", Locale.getDefault()).format(cal.time)
}

actual fun shareFile(content: String, mimeType: String, fileName: String) {
    val context = AndroidShareHelper.getContext()
    val file = File(context.cacheDir, fileName)
    
    // For CSV files, write UTF-8 BOM for Excel/LibreOffice compatibility
    if (mimeType == MimeTypes.TEXT_CSV) {
        file.outputStream().use { output ->
            output.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            output.write(content.toByteArray(Charsets.UTF_8))
        }
    } else {
        file.writeText(content, Charsets.UTF_8)
    }
    
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    
    // For HTML files, offer both VIEW and SEND options
    if (mimeType == MimeTypes.TEXT_HTML || mimeType == MimeTypes.TEXT_CSV) {
        // Primary intent: VIEW (open in browser)
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Additional intent: SEND (share)
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
    } else {
        // For TXT files, use ACTION_SEND to share
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        context.startActivity(Intent.createChooser(intent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

actual fun shareFileBytes(content: ByteArray, mimeType: String, fileName: String) {
    val context = AndroidShareHelper.getContext()
    val file = File(context.cacheDir, fileName)
    
    file.writeBytes(content)
    
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )
    
    // For XLSX files, offer both VIEW and SEND options
    if (mimeType == MimeTypes.APPLICATION_XLSX) {
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

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
    } else {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        context.startActivity(Intent.createChooser(intent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

private object AndroidShareHelper : KoinComponent {
    fun getContext(): Context {
        val context: Context by inject()
        return context
    }
}

actual suspend fun awaitFirestorePendingWrites() {
    Firebase.firestore.waitForPendingWrites().await()
}