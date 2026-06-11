package org.gmautostop.hitchlogmp.data

import com.russhwolf.settings.Settings

/**
 * Persists the id of the last opened log so the app can reopen it on launch.
 */
class LastOpenedLogPreferences(private val settings: Settings = Settings()) {

    fun load(): String? = settings.getStringOrNull(KEY_LAST_LOG_ID)

    fun save(logId: String) {
        settings.putString(KEY_LAST_LOG_ID, logId)
    }

    fun clear() {
        settings.remove(KEY_LAST_LOG_ID)
    }

    companion object {
        private const val KEY_LAST_LOG_ID = "last_opened_log_id"
    }
}
