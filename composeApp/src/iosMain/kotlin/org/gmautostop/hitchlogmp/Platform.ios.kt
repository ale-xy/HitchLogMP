package org.gmautostop.hitchlogmp

import kotlinx.datetime.LocalDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateComponents
import platform.Foundation.NSDateFormatter
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun formatDateLocale(date: LocalDate): String {
    val components = NSDateComponents().apply {
        year = date.year.toLong()
        month = date.monthNumber.toLong()
        day = date.dayOfMonth.toLong()
    }
    val nsDate = NSCalendar.currentCalendar.dateFromComponents(components)
        ?: return "${date.dayOfMonth}.${date.monthNumber}.${date.year}"
    return NSDateFormatter().apply { dateFormat = "d MMMM yyyy, EEEE" }.stringFromDate(nsDate)
}