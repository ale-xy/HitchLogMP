package org.gmautostop.hitchlogmp.domain

import no.synth.kmpzip.io.ByteArrayOutputStream
import no.synth.kmpzip.zip.ZipEntry
import no.synth.kmpzip.zip.ZipOutputStream

internal actual fun createZipArchive(files: Map<String, ByteArray>): ByteArray {
    val outputStream = ByteArrayOutputStream()
    ZipOutputStream(outputStream).use { zip ->
        files.forEach { (path, bytes) ->
            zip.putNextEntry(ZipEntry(path))
            zip.write(bytes)
            zip.closeEntry()
        }
    }
    return outputStream.toByteArray()
}
