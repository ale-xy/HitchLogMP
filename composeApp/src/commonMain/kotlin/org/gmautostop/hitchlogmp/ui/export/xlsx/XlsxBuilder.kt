package org.gmautostop.hitchlogmp.ui.export.xlsx

class XlsxSheetBuilder(private val name: String) {
    private val rows = mutableListOf<XlsxRow>()
    private val merges = mutableListOf<XlsxMerge>()
    
    fun headerRow(vararg values: String) {
        rows.add(XlsxRow(values.map { XlsxCell.Text(it) }))
    }
    
    fun dataRow(vararg values: Any?) {
        rows.add(XlsxRow(values.map { XlsxCell.from(it) }))
    }
    
    fun row(vararg cells: XlsxCell) {
        rows.add(XlsxRow(cells.toList()))
    }
    
    /**
     * Add a merged cell spanning multiple columns.
     * The row will contain the value in the first cell, and empty cells for the rest.
     * @param colSpan Number of columns to span
     * @param values Cell values (first value will be shown, rest should be empty strings)
     */
    fun mergedRow(colSpan: Int, vararg values: Any?) {
        val rowIndex = rows.size
        rows.add(XlsxRow(values.map { XlsxCell.from(it) }))
        
        // Add merge for the first cell spanning colSpan columns
        if (colSpan > 1) {
            merges.add(XlsxMerge(
                startRow = rowIndex,
                startCol = 0,
                endRow = rowIndex,
                endCol = colSpan - 1
            ))
        }
    }
    
    internal fun build(): XlsxSheet = XlsxSheet(name, rows, merges)
}

class XlsxWorkbookBuilder {
    private val sheets = mutableListOf<XlsxSheet>()
    
    fun sheet(name: String, block: XlsxSheetBuilder.() -> Unit) {
        val builder = XlsxSheetBuilder(name)
        builder.block()
        sheets.add(builder.build())
    }
    
    internal fun build(): XlsxWorkbook = XlsxWorkbook(sheets)
}

object XlsxBuilder {
    fun workbook(block: XlsxWorkbookBuilder.() -> Unit): XlsxWorkbook {
        val builder = XlsxWorkbookBuilder()
        builder.block()
        return builder.build()
    }
}
