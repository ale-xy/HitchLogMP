package org.gmautostop.hitchlogmp.export

/**
 * Platform-specific file saving interface.
 * Implementations handle file I/O and share sheet presentation on each platform.
 */
interface FileSaver {
    /**
     * Saves bytes to a file and presents platform-specific share/save UI.
     * 
     * @param fileName Name of the file to save (e.g., "MyLog.html")
     * @param bytes File content as byte array
     * @param format Export format (determines MIME type for sharing)
     */
    suspend fun save(fileName: String, bytes: ByteArray, format: ExportFormat)
}
