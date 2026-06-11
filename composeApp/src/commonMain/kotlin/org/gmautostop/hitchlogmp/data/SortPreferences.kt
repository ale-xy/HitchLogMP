package org.gmautostop.hitchlogmp.data

import com.russhwolf.settings.Settings
import org.gmautostop.hitchlogmp.ui.loglist.SortConfig
import org.gmautostop.hitchlogmp.ui.loglist.SortDirection
import org.gmautostop.hitchlogmp.ui.loglist.SortField

class SortPreferences(private val settings: Settings = Settings()) {

    fun load(): SortConfig {
        val field = settings.getStringOrNull(KEY_SORT_FIELD)
            ?.let { runCatching { SortField.valueOf(it) }.getOrNull() }
            ?: SortField.CREATION_DATE
        val direction = settings.getStringOrNull(KEY_SORT_DIRECTION)
            ?.let { runCatching { SortDirection.valueOf(it) }.getOrNull() }
            ?: SortDirection.DESCENDING
        return SortConfig(field, direction)
    }

    fun save(config: SortConfig) {
        settings.putString(KEY_SORT_FIELD, config.field.name)
        settings.putString(KEY_SORT_DIRECTION, config.direction.name)
    }

    companion object {
        private const val KEY_SORT_FIELD = "sort_field"
        private const val KEY_SORT_DIRECTION = "sort_direction"
    }
}
