package org.gmautostop.hitchlogmp.ui.export.xlsx

data class XlsxWorkbook(val sheets: List<XlsxSheet>) {
    init {
        require(sheets.isNotEmpty()) { "Workbook must have at least one sheet" }
        require(sheets.size <= 255) { "Workbook cannot have more than 255 sheets" }
        
        val duplicateNames = sheets.groupBy { it.name }.filter { it.value.size > 1 }
        require(duplicateNames.isEmpty()) { 
            "Duplicate sheet names: ${duplicateNames.keys.joinToString()}" 
        }
    }
}
