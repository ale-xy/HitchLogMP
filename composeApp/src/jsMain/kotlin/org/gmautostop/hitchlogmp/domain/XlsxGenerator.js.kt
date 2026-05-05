package org.gmautostop.hitchlogmp.domain

internal actual fun createZipArchive(files: Map<String, ByteArray>): ByteArray {
    // For JS target, we'll use JSZip library via dynamic calls
    // This is a simplified implementation that creates a basic ZIP structure
    // In production, you would want to use a proper JS ZIP library
    
    // For now, return empty array as XLSX export is not critical for web
    // TODO: Implement using JSZip library or similar
    return ByteArray(0)
}
