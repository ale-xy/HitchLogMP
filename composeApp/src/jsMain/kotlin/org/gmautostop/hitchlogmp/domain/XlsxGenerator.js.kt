package org.gmautostop.hitchlogmp.domain

import kotlinx.coroutines.await
import org.khronos.webgl.Uint8Array
import org.khronos.webgl.get

actual suspend fun createZipArchive(files: Map<String, ByteArray>): ByteArray {
    val zip = JSZip()
    
    // Add each file to the ZIP
    files.forEach { (path, bytes) ->
        // Convert Kotlin ByteArray to JS Uint8Array
        val uint8Array = Uint8Array(bytes.size)
        for (i in bytes.indices) {
            uint8Array.asDynamic()[i] = bytes[i].toInt() and 0xFF
        }
        zip.file(path, uint8Array)
    }
    
    // Generate ZIP as Uint8Array
    val options = js("({ type: 'uint8array' })")
    val result = zip.generateAsync(options).await()
    
    // Convert JS Uint8Array back to Kotlin ByteArray
    val uint8Result = result.unsafeCast<Uint8Array>()
    return ByteArray(uint8Result.length) { i ->
        uint8Result[i].toByte()
    }
}
