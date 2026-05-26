package org.gmautostop.hitchlogmp.export

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication

@OptIn(ExperimentalForeignApi::class)
class IosFileSaver : FileSaver {
    override suspend fun save(fileName: String, bytes: ByteArray, format: ExportFormat) {
        // Write to temp directory
        val tempDir = NSTemporaryDirectory()
        val filePath = "$tempDir$fileName"
        
        // Convert ByteArray to NSData
        val data = bytes.usePinned { pinned ->
            NSData.create(
                bytes = pinned.addressOf(0),
                length = bytes.size.toULong()
            )
        }
        
        data.writeToFile(filePath, atomically = true)
        
        // Present UIActivityViewController
        val fileURL = NSURL.fileURLWithPath(filePath)
        val activityVC = UIActivityViewController(
            activityItems = listOf(fileURL),
            applicationActivities = null
        )
        
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVC?.presentViewController(activityVC, animated = true, completion = null)
    }
}
