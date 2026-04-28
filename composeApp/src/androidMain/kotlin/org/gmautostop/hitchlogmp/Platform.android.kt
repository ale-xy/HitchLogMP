package org.gmautostop.hitchlogmp

import android.os.Build
import kotlinx.datetime.LocalDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun formatDateLocale(date: LocalDate): String {
    val cal = Calendar.getInstance().also {
        it.set(date.year, date.monthNumber - 1, date.dayOfMonth)
    }
    return SimpleDateFormat("d MMMM yyyy, EEEE", Locale.getDefault()).format(cal.time)
}