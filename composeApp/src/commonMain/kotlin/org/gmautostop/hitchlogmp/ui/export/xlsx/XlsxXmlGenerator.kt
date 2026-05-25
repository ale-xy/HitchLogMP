package org.gmautostop.hitchlogmp.ui.export.xlsx

import org.gmautostop.hitchlogmp.domain.getExcelColumnName

object XlsxXmlGenerator {
    
    fun generateXmlFiles(workbook: XlsxWorkbook): Map<String, ByteArray> {
        val sharedStrings = mutableListOf<String>()
        val stringMap = mutableMapOf<String, Int>()
        
        fun getStringIndex(value: String): Int {
            return stringMap.getOrPut(value) {
                sharedStrings.add(value)
                sharedStrings.size - 1
            }
        }
        
        // Generate sheet XMLs
        val sheetXmls = workbook.sheets.mapIndexed { index, sheet ->
            val sheetNum = index + 1
            val xml = generateSheetXml(sheet, ::getStringIndex)
            "xl/worksheets/sheet$sheetNum.xml" to xml.encodeToByteArray()
        }
        
        // Generate other files
        val files = mutableMapOf<String, ByteArray>()
        files["[Content_Types].xml"] = generateContentTypes(workbook.sheets.size).encodeToByteArray()
        files["_rels/.rels"] = generateRootRels().encodeToByteArray()
        files["xl/workbook.xml"] = generateWorkbook(workbook).encodeToByteArray()
        files["xl/_rels/workbook.xml.rels"] = generateWorkbookRels(workbook.sheets.size).encodeToByteArray()
        files["xl/sharedStrings.xml"] = generateSharedStrings(sharedStrings).encodeToByteArray()
        files.putAll(sheetXmls)
        
        return files
    }
    
    private fun generateSheetXml(sheet: XlsxSheet, getStringIndex: (String) -> Int): String {
        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
            append("""<sheetData>""")
            
            sheet.rows.forEachIndexed { rowIndex, row ->
                val excelRow = rowIndex + 1
                append("""<row r="$excelRow">""")
                
                row.cells.forEachIndexed { colIndex, cell ->
                    val cellRef = "${getExcelColumnName(colIndex)}$excelRow"
                    when (cell) {
                        is XlsxCell.Text -> {
                            val strIdx = getStringIndex(cell.value)
                            append("""<c r="$cellRef" t="s"><v>$strIdx</v></c>""")
                        }
                        is XlsxCell.Number -> {
                            append("""<c r="$cellRef" t="n"><v>${cell.value}</v></c>""")
                        }
                    }
                }
                
                append("""</row>""")
            }
            
            append("""</sheetData>""")
            
            // Add mergeCells section if there are any merges
            if (sheet.merges.isNotEmpty()) {
                append("""<mergeCells count="${sheet.merges.size}">""")
                sheet.merges.forEach { merge ->
                    val startCell = "${getExcelColumnName(merge.startCol)}${merge.startRow + 1}"
                    val endCell = "${getExcelColumnName(merge.endCol)}${merge.endRow + 1}"
                    append("""<mergeCell ref="$startCell:$endCell"/>""")
                }
                append("""</mergeCells>""")
            }
            
            append("""</worksheet>""")
        }
    }
    
    private fun generateContentTypes(sheetCount: Int): String {
        val sheetOverrides = (1..sheetCount).joinToString("\n") { sheetNum ->
            """    <Override PartName="/xl/worksheets/sheet$sheetNum.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>"""
        }
        
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
$sheetOverrides
    <Override PartName="/xl/sharedStrings.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sharedStrings+xml"/>
</Types>""".trimIndent()
    }
    
    private fun generateRootRels(): String {
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>""".trimIndent()
    }
    
    private fun generateWorkbook(workbook: XlsxWorkbook): String {
        val sheets = workbook.sheets.mapIndexed { index, sheet ->
            val sheetId = index + 1
            val rId = "rId$sheetId"
            val safeName = escapeXml(sheet.name)
            """        <sheet name="$safeName" sheetId="$sheetId" r:id="$rId"/>"""
        }.joinToString("\n")
        
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <sheets>
$sheets
    </sheets>
</workbook>""".trimIndent()
    }
    
    private fun generateWorkbookRels(sheetCount: Int): String {
        val sheetRels = (1..sheetCount).joinToString("\n") { sheetNum ->
            """    <Relationship Id="rId$sheetNum" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$sheetNum.xml"/>"""
        }
        val sharedStringsRId = sheetCount + 1
        
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
$sheetRels
    <Relationship Id="rId$sharedStringsRId" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings" Target="sharedStrings.xml"/>
</Relationships>""".trimIndent()
    }
    
    private fun generateSharedStrings(strings: List<String>): String {
        val items = strings.joinToString("") { str ->
            val safeStr = escapeXml(str)
            """<si><t>$safeStr</t></si>"""
        }
        
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<sst xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" count="${strings.size}" uniqueCount="${strings.size}">
$items
</sst>""".trimIndent()
    }
    
    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
