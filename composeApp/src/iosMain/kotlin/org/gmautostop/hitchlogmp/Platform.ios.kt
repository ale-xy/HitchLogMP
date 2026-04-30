package org.gmautostop.hitchlogmp

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
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
import platform.UIKit.UIViewController

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun formatDateLocale(date: LocalDate): String {
    val components = NSDateComponents().apply {
        year = date.year.toLong()
        month = date.monthNumber.toLong()
        day = date.dayOfMonth.toLong()
    }
    val nsDate = NSCalendar.currentCalendar.dateFromComponents(components)
        ?: return "${date.dayOfMonth}.${date.monthNumber}.${date.year}"
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
    val rootVC = keyWindow?.rootViewController() as? UIViewController
    
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
    val rootVC = keyWindow?.rootViewController() as? UIViewController
    
    rootVC?.presentViewController(activityVC, animated = true, completion = null)
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData {
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = this.size.toULong())
    }
}