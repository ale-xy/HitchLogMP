package org.gmautostop.hitchlogmp

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun Instant.localTZDateTime() =
    toLocalDateTime(TimeZone.currentSystemDefault())

fun Timestamp.toLocalDateTime() =
    Instant.fromEpochSeconds(seconds).localTZDateTime()

fun LocalDateTime.toTimestamp() =
    Timestamp(toInstant(TimeZone.currentSystemDefault()).epochSeconds, 0)

@OptIn(FormatStringsInDatetimeFormats::class)
val dateFormat = LocalDate.Format { byUnicodePattern("dd.MM.yyyy") }

@OptIn(FormatStringsInDatetimeFormats::class)
val timeFormat = LocalTime.Format { byUnicodePattern("HH:mm") }

@OptIn(FormatStringsInDatetimeFormats::class)
val dateTimeFormat = LocalDateTime.Format { byUnicodePattern("dd.MM.yyyy HH:mm") }

@OptIn(FormatStringsInDatetimeFormats::class)
val timeFormatForDisplay = LocalDateTime.Format { byUnicodePattern("HH:mm") }

//fun LocalDate.minusDays(days: Int): LocalDate = this.minus(DatePeriod(days = days))
