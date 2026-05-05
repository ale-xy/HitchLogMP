package org.gmautostop.hitchlogmp

import androidx.compose.runtime.Composable
import cocoapods.FirebaseFirestore.FIRFirestore
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSData
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun formatDateLocale(date: LocalDate): String {
    val components = NSDateComponents().apply {
        year = date.year.toLong()
        month = date.month.value.toLong()
        day = date.dayOfMonth.toLong()
    }
    val nsDate = NSCalendar.currentCalendar.dateFromComponents(components)
        ?: return "${date.dayOfMonth}.${date.month.value}.${date.year}"
    return NSDateFormatter().apply { dateFormat = "d MMMM yyyy, EEEE" }.stringFromDate(nsDate)
}

@OptIn(ExperimentalForeignApi::class)
actual fun shareFile(content: String, mimeType: String, fileName: String) {
    val filePath = NSTemporaryDirectory() + fileName
    val data = content.encodeToByteArray().toNSData()
    data.writeToFile(filePath, atomically = true)
    
    val url = NSURL.fileURLWithPath(filePath)
    val activityVC = UIActivityViewController(listOf(url), null)
    
    // Get the key window and present from its root view controller
    val keyWindow = UIApplication.sharedApplication.keyWindow
    val rootVC = keyWindow?.rootViewController()
    
    rootVC?.presentViewController(activityVC, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
actual fun shareFileBytes(content: ByteArray, mimeType: String, fileName: String) {
    val filePath = NSTemporaryDirectory() + fileName
    val data = content.toNSData()
    data.writeToFile(filePath, atomically = true)
    
    val url = NSURL.fileURLWithPath(filePath)
    val activityVC = UIActivityViewController(listOf(url), null)
    
    // Get the key window and present from its root view controller
    val keyWindow = UIApplication.sharedApplication.keyWindow
    val rootVC = keyWindow?.rootViewController()
    
    rootVC?.presentViewController(activityVC, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}

actual suspend fun awaitFirestorePendingWrites() = suspendCancellableCoroutine { cont ->
    FIRFirestore.firestore().waitForPendingWritesWithCompletion { error ->
        if (error != null) cont.resumeWithException(Exception(error.localizedDescription))
        else cont.resume(Unit)
    }
}

actual fun isGoogleAuthUiSupported(): Boolean = true

@Composable
actual fun androidx.compose.ui.Modifier.platformWindowInsetsPadding(): androidx.compose.ui.Modifier {
    return this.windowInsetsPadding(androidx.compose.foundation.layout.WindowInsets.safeDrawing)
}