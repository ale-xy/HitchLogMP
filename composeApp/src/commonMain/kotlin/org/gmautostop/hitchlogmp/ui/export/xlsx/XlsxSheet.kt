package org.gmautostop.hitchlogmp.ui.export.xlsx

data class XlsxRow(val cells: List<XlsxCell>)

data class XlsxMerge(
    val startRow: Int,  // 0-based
    val startCol: Int,  // 0-based
    val endRow: Int,    // 0-based
    val endCol: Int     // 0-based
)

data class XlsxSheet(
    val name: String,
    val rows: List<XlsxRow>,
    val merges: List<XlsxMerge> = emptyList()
) {
    init {
        require(name.isNotBlank()) { "Sheet name cannot be blank" }
        require(name.length <= 31) { "Sheet name cannot exceed 31 characters" }
        require(!name.contains(Regex("[\\[\\]\\*\\?/\\\\]"))) { 
            "Sheet name cannot contain: [ ] * ? / \\" 
        }
    }
}
