package org.gmautostop.hitchlogmp.domain

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.serializer
import no.synth.kmpzip.io.ByteArrayOutputStream
import no.synth.kmpzip.zip.ZipEntry
import no.synth.kmpzip.zip.ZipOutputStream

/**
 * Generates XLSX file bytes from a list of data objects.
 * Uses @ExcelColumn annotations to determine column headers and order.
 */
@OptIn(ExperimentalSerializationApi::class)
internal inline fun <reified T> generateXlsxBytes(data: List<T>): ByteArray {
    val descriptor = serializer<T>().descriptor
    val headers = mutableListOf<String>()
    val propertyNames = mutableListOf<String>()
    
    // Extract headers and property names from @ExcelColumn annotations
    for (i in 0 until descriptor.elementsCount) {
        val propName = descriptor.getElementName(i)
        val excelAnno = descriptor.getElementAnnotations(i).filterIsInstance<ExcelColumn>().firstOrNull()
        
        if (excelAnno?.ignore != true) {
            headers.add(excelAnno?.name ?: propName)
            propertyNames.add(propName)
        }
    }
    
    // Convert data to string matrix via JSON serialization
    val json = Json { encodeDefaults = true }
    val dataMatrix = data.map { item ->
        val jsonElement = json.encodeToJsonElement(serializer<T>(), item)
        val jsonObject = jsonElement as? JsonObject ?: return@map emptyList()
        propertyNames.map { prop ->
            (jsonObject[prop] as? JsonPrimitive)?.content ?: ""
        }
    }
    
    val xlsxFiles = createXlsxFiles(headers, dataMatrix)
    return createZipArchive(xlsxFiles)
}

/**
 * Creates the internal XML files that comprise an XLSX workbook.
 * Returns a map of file paths to their byte content.
 */
internal fun createXlsxFiles(headers: List<String>, dataMatrix: List<List<String>>): Map<String, ByteArray> {
    val sharedStrings = mutableListOf<String>()
    val stringMap = mutableMapOf<String, Int>()
    
    fun getStringIndex(value: String): Int {
        return stringMap.getOrPut(value) {
            sharedStrings.add(value)
            sharedStrings.size - 1
        }
    }
    
    val sheetXml = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        append("""<sheetData>""")
        
        // Write Headers (Row 1)
        append("""<row r="1">""")
        headers.forEachIndexed { colIndex, header ->
            val cellRef = "${getExcelColumnName(colIndex)}1"
            val strIdx = getStringIndex(header)
            append("""<c r="$cellRef" t="s"><v>$strIdx</v></c>""")
        }
        append("""</row>""")
        
        // Write data rows
        dataMatrix.forEachIndexed { rowIndex, rowData ->
            val excelRow = rowIndex + 2
            append("""<row r="$excelRow">""")
            rowData.forEachIndexed { colIndex, value ->
                val cellRef = "${getExcelColumnName(colIndex)}$excelRow"
                val doubleVal = value.toDoubleOrNull()
                if (doubleVal != null) {
                    append("""<c r="$cellRef" t="n"><v>$value</v></c>""")
                } else {
                    val strIdx = getStringIndex(value)
                    append("""<c r="$cellRef" t="s"><v>$strIdx</v></c>""")
                }
            }
            append("""</row>""")
        }
        
        append("""</sheetData></worksheet>""")
    }
    
    val sharedStringsXml = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="${sharedStrings.size}" uniqueCount="${sharedStrings.size}">""")
        sharedStrings.forEach { str ->
            val safeStr = str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
            append("""<si><t>$safeStr</t></si>""")
        }
        append("""</sst>""")
    }
    
    val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
</Types>""".trimIndent()
    
    val rootRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".trimIndent()
    
    val workbook = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <sheets>
        <sheet name="Sheet1" sheetId="1" r:id="rId1"/>
    </sheets>
</workbook>""".trimIndent()
    
    val workbookRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
</Relationships>""".trimIndent()
    
    return mapOf(
        "[Content_Types].xml" to contentTypes.encodeToByteArray(),
        "_rels/.rels" to rootRels.encodeToByteArray(),
        "xl/workbook.xml" to workbook.encodeToByteArray(),
        "xl/_rels/workbook.xml.rels" to workbookRels.encodeToByteArray(),
        "xl/sharedStrings.xml" to sharedStringsXml.encodeToByteArray(),
        "xl/worksheets/sheet1.xml" to sheetXml.encodeToByteArray()
    )
}

/**
 * Converts a zero-based column index to Excel column name (A, B, ..., Z, AA, AB, ...).
 */
internal fun getExcelColumnName(columnIndex: Int): String {
    var index = columnIndex
    var columnName = ""
    while (index >= 0) {
        columnName = ('A' + (index % 26)) + columnName
        index = (index / 26) - 1
    }
    return columnName
}

/**
 * Creates a ZIP archive from a map of file paths to byte arrays using kmp-zip library.
 */
private fun createZipArchive(files: Map<String, ByteArray>): ByteArray {
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
