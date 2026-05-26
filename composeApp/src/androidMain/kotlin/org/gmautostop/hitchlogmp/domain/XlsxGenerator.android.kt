package org.gmautostop.hitchlogmp.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.synth.kmpzip.io.ByteArrayOutputStream
import no.synth.kmpzip.zip.ZipEntry
import no.synth.kmpzip.zip.ZipOutputStream

actual suspend fun createZipArchive(files: Map<String, ByteArray>): ByteArray {
    return withContext(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        ZipOutputStream(outputStream).use { zip ->
            files.forEach { (path, bytes) ->
                zip.putNextEntry(ZipEntry(path))
                zip.write(bytes)
                zip.closeEntry()
            }
        }
        outputStream.toByteArray()
    }
}
