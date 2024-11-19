package org.gmautostop.hitchlogmp

import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.toMilliseconds
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

fun Instant.localTZDateTime() =
    toLocalDateTime(TimeZone.currentSystemDefault())

fun Timestamp.toLocalDateTime() =
    Instant.fromEpochSeconds(seconds).localTZDateTime()

fun LocalDateTime.toTimestamp() =
    Timestamp(toInstant(TimeZone.currentSystemDefault()).epochSeconds, 0)