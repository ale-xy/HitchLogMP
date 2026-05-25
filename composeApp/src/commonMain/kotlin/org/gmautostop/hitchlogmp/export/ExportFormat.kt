package org.gmautostop.hitchlogmp.export

sealed class ExportFormat(val mimeType: String, val extension: String) {
    data object Text : ExportFormat("text/plain", "txt")
    data object Csv : ExportFormat("text/csv", "csv")
    data object Html : ExportFormat("text/html", "html")
    data object Xlsx : ExportFormat("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx")
}
