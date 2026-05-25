package org.gmautostop.hitchlogmp.ui.export.xlsx

sealed class XlsxCell {
    data class Text(val value: String) : XlsxCell()
    data class Number(val value: Double) : XlsxCell()
    
    companion object {
        fun from(value: Any?): XlsxCell = when (value) {
            null -> Text("")
            is String -> Text(value)
            is Int -> Number(value.toDouble())
            is Long -> Number(value.toDouble())
            is Double -> Number(value)
            is Float -> Number(value.toDouble())
            else -> Text(value.toString())
        }
    }
}
