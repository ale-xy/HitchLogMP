package org.gmautostop.hitchlogmp.domain.logic

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * The time a record is stored with, so that records sharing a minute keep the order they were added in.
 *
 * Users enter times as HH:mm. If [entered]'s minute already has records, the new record is stored
 * one second after the latest of them; otherwise it is stored as entered.
 * The seconds only encode order — durations must ignore them (see [minutesBetween]).
 *
 * @param entered Time entered by the user
 * @param existing Times of other records in the log; only those in [entered]'s minute are considered.
 *   When editing, the edited record itself must be excluded.
 */
fun storedTimeFor(entered: LocalDateTime, existing: List<LocalDateTime>): LocalDateTime {
    val latestInMinute = existing.filter { isSameMinute(it, entered) }.maxOrNull() ?: return entered
    return latestInMinute.toInstant(TimeZone.UTC).plus(1, DateTimeUnit.SECOND).toLocalDateTime(TimeZone.UTC)
}
