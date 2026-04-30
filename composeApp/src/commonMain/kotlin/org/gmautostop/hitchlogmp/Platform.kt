package org.gmautostop.hitchlogmp

import kotlinx.datetime.LocalDate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun formatDateLocale(date: LocalDate): String

expect fun shareFile(content: String, mimeType: String, fileName: String)