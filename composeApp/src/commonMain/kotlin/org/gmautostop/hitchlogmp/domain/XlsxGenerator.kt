package org.gmautostop.hitchlogmp.domain

/**
 * Converts a zero-based column index to Excel column name (A, B, ..., Z, AA, AB, ...).
 */
fun getExcelColumnName(columnIndex: Int): String {
    var index = columnIndex
    var columnName = ""
    while (index >= 0) {
        columnName = ('A' + (index % 26)) + columnName
        index = (index / 26) - 1
    }
    return columnName
}

/**
 * Creates a ZIP archive from a map of file paths to byte arrays.
 * Platform-specific implementation.
 */
expect fun createZipArchive(files: Map<String, ByteArray>): ByteArray
