package org.gmautostop.hitchlogmp.ui.export.xlsx

import org.gmautostop.hitchlogmp.domain.createZipArchive

object XlsxArchiver {
    suspend fun createXlsxBytes(workbook: XlsxWorkbook): ByteArray {
        val xmlFiles = XlsxXmlGenerator.generateXmlFiles(workbook)
        return createZipArchive(xmlFiles)
    }
}
