package org.gmautostop.hitchlogmp.domain

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.until

enum class LiveStatus { IN_CAR, REST, OFFSIDE, FINISH, RETIRE }

data class LiveState(val status: LiveStatus, val since: LocalDateTime?)

fun computeLiveState(records: List<HitchLogRecord>): LiveState? {
    val sorted = records.sortedBy { it.time }
    var inCar: LocalDateTime? = null
    var rest: LocalDateTime? = null
    var offside: LocalDateTime? = null
    for (r in sorted) {
        when (r.type) {
            HitchLogRecordType.LIFT -> inCar = r.time
            HitchLogRecordType.GET_OFF -> inCar = null
            HitchLogRecordType.REST_ON -> rest = r.time
            HitchLogRecordType.REST_OFF -> rest = null
            HitchLogRecordType.OFFSIDE_ON -> offside = r.time
            HitchLogRecordType.OFFSIDE_OFF -> offside = null
            else -> {}
        }
    }
    val finished = sorted.any { it.type == HitchLogRecordType.FINISH }
    val retired = sorted.any { it.type == HitchLogRecordType.RETIRE }
    return when {
        finished -> LiveState(LiveStatus.FINISH, null)
        retired -> LiveState(LiveStatus.RETIRE, null)
        offside != null -> LiveState(LiveStatus.OFFSIDE, offside)
        rest != null -> LiveState(LiveStatus.REST, rest)
        inCar != null -> LiveState(LiveStatus.IN_CAR, inCar)
        else -> null
    }
}

fun computeRestMinutes(records: List<HitchLogRecord>): Int {
    val sorted = records.sortedBy { it.time }
    var total = 0L
    var onAt: LocalDateTime? = null
    for (r in sorted) {
        when (r.type) {
            HitchLogRecordType.REST_ON -> onAt = r.time
            HitchLogRecordType.REST_OFF -> if (onAt != null) {
                total += minutesBetween(onAt, r.time)
                onAt = null
            }
            else -> {}
        }
    }
    if (onAt != null && sorted.isNotEmpty()) {
        total += minutesBetween(onAt, sorted.last().time)
    }
    return total.toInt().coerceAtLeast(0)
}

/**
 * Computes the number of completed rest divisions (REST_ON/REST_OFF pairs).
 * Only counts pairs where both ON and OFF exist.
 * Active rest (REST_ON without REST_OFF) is not counted.
 */
fun computeRestDivisions(records: List<HitchLogRecord>): Int {
    val sorted = records.sortedBy { it.time }
    var count = 0
    var hasRestOn = false
    for (r in sorted) {
        when (r.type) {
            HitchLogRecordType.REST_ON -> hasRestOn = true
            HitchLogRecordType.REST_OFF -> {
                if (hasRestOn) {
                    count++
                    hasRestOn = false
                }
            }
            else -> {}
        }
    }
    return count
}

/**
 * Computes remaining rest time.
 * Returns 0 until total rest time feature is implemented.
 * 
 * @param records All log records
 * @param totalRestMin Total rest time allowed (null if not configured)
 * @return Remaining rest minutes
 */
fun computeRestLeft(records: List<HitchLogRecord>, totalRestMin: Int?): Int {
    if (totalRestMin == null) return 0
    val used = computeRestMinutes(records)
    return (totalRestMin - used).coerceAtLeast(0)
}

/**
 * Computes remaining rest divisions.
 * Returns 0 until total rest divisions feature is implemented.
 * 
 * @param records All log records
 * @param totalRestDivisions Total divisions allowed (null if not configured)
 * @return Remaining divisions
 */
fun computeRestDivisionsLeft(records: List<HitchLogRecord>, totalRestDivisions: Int?): Int {
    if (totalRestDivisions == null) return 0
    val used = computeRestDivisions(records)
    return (totalRestDivisions - used).coerceAtLeast(0)
}

private fun minutesBetween(from: LocalDateTime, to: LocalDateTime): Long {
    val fromSec = from.date.toEpochDays() * 86400 + from.hour * 3600 + from.minute * 60 + from.second
    val toSec = to.date.toEpochDays() * 86400 + to.hour * 3600 + to.minute * 60 + to.second
    return ((toSec - fromSec) / 60).coerceAtLeast(0)
}

fun formatMinutes(min: Int): String =
    "${(min / 60).toString().padStart(2, '0')}:${(min % 60).toString().padStart(2, '0')}"

/**
 * Computes live rest minutes including ongoing rest period.
 * If currently on rest, adds elapsed time since REST_ON to the total.
 * 
 * @param records All log records
 * @param currentTime Current time for live calculation
 * @return Total rest minutes including live elapsed time
 */
fun computeLiveRestMinutes(records: List<HitchLogRecord>, currentTime: LocalDateTime): Int {
    val baseMinutes = computeRestMinutes(records)
    val liveState = computeLiveState(records)
    
    if (liveState?.status == LiveStatus.REST && liveState.since != null) {
        val elapsed = liveState.since.toInstant(TimeZone.currentSystemDefault())
            .until(currentTime.toInstant(TimeZone.currentSystemDefault()), DateTimeUnit.MINUTE)
        return baseMinutes + elapsed.toInt()
    }
    
    return baseMinutes
}

private val NEUTRAL_TYPES = setOf(
    HitchLogRecordType.MEET,
    HitchLogRecordType.CHECKPOINT,
    HitchLogRecordType.FREE_TEXT,
)

fun nextActionLadder(records: List<HitchLogRecord>): List<HitchLogRecordType> {
    if (records.isEmpty()) return ladderFor(null)
    val sorted = records.sortedBy { it.time }
    val lastType = sorted.last().type
    if (lastType !in NEUTRAL_TYPES) return ladderFor(lastType)
    val prevNonNeutral = sorted.dropLast(1).lastOrNull { it.type !in NEUTRAL_TYPES }?.type
    return ladderFor(prevNonNeutral)
}

private fun ladderFor(type: HitchLogRecordType?): List<HitchLogRecordType> {
    val base = listOf(
        HitchLogRecordType.LIFT, HitchLogRecordType.GET_OFF,
        HitchLogRecordType.CHECKPOINT, HitchLogRecordType.WALK,
        HitchLogRecordType.MEET, HitchLogRecordType.REST_ON,
        HitchLogRecordType.FREE_TEXT, HitchLogRecordType.FINISH,
    )
    return when (type) {
        HitchLogRecordType.START -> listOf(HitchLogRecordType.LIFT, HitchLogRecordType.WALK, HitchLogRecordType.CHECKPOINT, HitchLogRecordType.MEET, HitchLogRecordType.FREE_TEXT)
        HitchLogRecordType.GET_OFF -> listOf(HitchLogRecordType.LIFT, HitchLogRecordType.WALK, HitchLogRecordType.CHECKPOINT, HitchLogRecordType.REST_ON, HitchLogRecordType.MEET, HitchLogRecordType.FREE_TEXT)
        HitchLogRecordType.WALK_END -> listOf(HitchLogRecordType.LIFT, HitchLogRecordType.CHECKPOINT, HitchLogRecordType.REST_ON, HitchLogRecordType.MEET, HitchLogRecordType.FREE_TEXT)
        HitchLogRecordType.LIFT -> listOf(HitchLogRecordType.GET_OFF, HitchLogRecordType.CHECKPOINT, HitchLogRecordType.MEET, HitchLogRecordType.REST_ON, HitchLogRecordType.FREE_TEXT)
        HitchLogRecordType.WALK -> listOf(HitchLogRecordType.WALK_END, HitchLogRecordType.LIFT, HitchLogRecordType.CHECKPOINT, HitchLogRecordType.MEET, HitchLogRecordType.FREE_TEXT)
        HitchLogRecordType.REST_ON -> listOf(HitchLogRecordType.REST_OFF, HitchLogRecordType.FREE_TEXT, HitchLogRecordType.MEET)
        HitchLogRecordType.REST_OFF -> listOf(HitchLogRecordType.LIFT, HitchLogRecordType.WALK, HitchLogRecordType.CHECKPOINT, HitchLogRecordType.FREE_TEXT)
        HitchLogRecordType.OFFSIDE_ON -> listOf(HitchLogRecordType.OFFSIDE_OFF, HitchLogRecordType.FREE_TEXT)
        HitchLogRecordType.OFFSIDE_OFF -> listOf(HitchLogRecordType.LIFT, HitchLogRecordType.CHECKPOINT, HitchLogRecordType.FREE_TEXT)
        HitchLogRecordType.FINISH -> listOf(HitchLogRecordType.FREE_TEXT)
        else -> base
    }
}
