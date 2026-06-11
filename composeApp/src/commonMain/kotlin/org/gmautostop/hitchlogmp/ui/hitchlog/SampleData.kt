package org.gmautostop.hitchlogmp.ui.hitchlog

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.gmautostop.hitchlogmp.domain.AppError
import org.gmautostop.hitchlogmp.domain.User
import org.gmautostop.hitchlogmp.domain.logic.computeLiveState
import org.gmautostop.hitchlogmp.domain.logic.computeRestDivisions
import org.gmautostop.hitchlogmp.domain.logic.computeRestDivisionsLeft
import org.gmautostop.hitchlogmp.domain.logic.computeRestLeft
import org.gmautostop.hitchlogmp.domain.logic.computeRestMinutes
import org.gmautostop.hitchlogmp.domain.logic.formatMinutes
import org.gmautostop.hitchlogmp.domain.logic.nextActionLadder
import org.gmautostop.hitchlogmp.domain.model.HitchLog
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.ViewState
import kotlin.time.Duration.Companion.minutes

// Sample Users
fun sampleUser(
    id: String = "user123",
    isAnonymous: Boolean = false
) = User(id = id, isAnonymous = isAnonymous)

fun sampleAnonymousUser() = User(id = "anon456", isAnonymous = true)

// Sample HitchLogs
fun sampleHitchLog(
    id: String = "log001",
    userId: String = "user123",
    name: String = "Москва → Санкт-Петербург",
    raceId: String = "race2026",
    teamId: String = "",
    team: String? = "Иванов + Сидорова",
    comment: String? = "Тренировочная гонка, плохая погода",
) = HitchLog(
    id = id,
    userId = userId,
    raceId = raceId,
    teamId = teamId,
    name = name,
    creationTime = Timestamp.now(),
    team = team,
    comment = comment,
)

fun sampleHitchLogs() = listOf(
    sampleHitchLog(id = "log001", name = "Москва → Санкт-Петербург"),
    sampleHitchLog(id = "log002", name = "Казань → Екатеринбург", teamId = "team5"),
    sampleHitchLog(id = "log003", name = "Новосибирск → Иркутск"),
    sampleHitchLog(id = "log004", name = "Владивосток → Хабаровск", teamId = "team12"),
)

// Sample HitchLogRecords
private val baseTime = LocalDateTime(2026, 4, 28, 10, 0, 0)

fun sampleRecord(
    id: String = "rec001",
    type: HitchLogRecordType = HitchLogRecordType.START,
    text: String = "",
    offsetMinutes: Int = 0
) = HitchLogRecord(
    id = id,
    time = baseTime.toInstant(TimeZone.currentSystemDefault())
        .plus(offsetMinutes.minutes)
        .toLocalDateTime(TimeZone.currentSystemDefault()),
    type = type,
    text = text
)

// Comprehensive record set for a full race
fun sampleHitchLogRecords() = listOf(
    sampleRecord("r1", HitchLogRecordType.START, "Старт от метро Сокол", 0),
    sampleRecord("r2", HitchLogRecordType.LIFT, "Газель, Владимир", 15),
    sampleRecord("r3", HitchLogRecordType.CHECKPOINT, "КП1 - Владимир", 180),
    sampleRecord("r4", HitchLogRecordType.GET_OFF, "Трасса М7", 185),
    sampleRecord("r5", HitchLogRecordType.WALK, "", 190),
    sampleRecord("r6", HitchLogRecordType.WALK_END, "", 210),
    sampleRecord("r7", HitchLogRecordType.LIFT, "Камаз, дальнобойщик Сергей", 215),
    sampleRecord("r8", HitchLogRecordType.MEET, "Встретили команду №7", 300),
    sampleRecord("r9", HitchLogRecordType.GET_OFF, "Нижний Новгород", 420),
    sampleRecord("r10", HitchLogRecordType.REST_ON, "Отдых у заправки", 425),
    sampleRecord("r11", HitchLogRecordType.REST_OFF, "", 545),
    sampleRecord("r12", HitchLogRecordType.LIFT, "Лада, местный житель", 550),
    sampleRecord("r13", HitchLogRecordType.CHECKPOINT, "КП2 - Казань", 720),
    sampleRecord("r14", HitchLogRecordType.FINISH, "Финиш!", 900),
)

// Minimal record set
fun sampleMinimalRecords() = listOf(
    sampleRecord("r1", HitchLogRecordType.START, "Старт", 0),
    sampleRecord("r2", HitchLogRecordType.LIFT, "Первая машина", 10),
)

// In-car state
fun sampleInCarRecords() = listOf(
    sampleRecord("r1", HitchLogRecordType.START, "", 0),
    sampleRecord("r2", HitchLogRecordType.LIFT, "Едем в машине", 30),
)

// Rest state
fun sampleRestRecords() = listOf(
    sampleRecord("r1", HitchLogRecordType.START, "", 0),
    sampleRecord("r2", HitchLogRecordType.LIFT, "", 30),
    sampleRecord("r3", HitchLogRecordType.GET_OFF, "", 120),
    sampleRecord("r4", HitchLogRecordType.REST_ON, "Отдых", 125),
)

// Offside state
fun sampleOffsideRecords() = listOf(
    sampleRecord("r1", HitchLogRecordType.START, "", 0),
    sampleRecord("r2", HitchLogRecordType.OFFSIDE_ON, "Вне игры", 60),
)

// Finished state
fun sampleFinishedRecords() = listOf(
    sampleRecord("r1", HitchLogRecordType.START, "", 0),
    sampleRecord("r2", HitchLogRecordType.LIFT, "", 30),
    sampleRecord("r3", HitchLogRecordType.CHECKPOINT, "КП1", 120),
    sampleRecord("r4", HitchLogRecordType.FINISH, "Финиш!", 240),
)

// Retired state
fun sampleRetiredRecords() = listOf(
    sampleRecord("r1", HitchLogRecordType.START, "", 0),
    sampleRecord("r2", HitchLogRecordType.LIFT, "", 30),
    sampleRecord("r3", HitchLogRecordType.RETIRE, "Сход с дистанции", 120),
)

// Sample ViewStates
fun <T : Any> sampleLoadingState() = ViewState.Loading
fun <T : Any> sampleShowState(value: T) = ViewState.Show(value)
fun <T : Any> sampleErrorState(message: String = "Ошибка загрузки данных") = 
    ViewState.Error(AppError.NetworkError(message))

// Sample HitchLogState
fun sampleHitchLogState(
    log: HitchLog = sampleHitchLog(),
    records: List<HitchLogRecord> = sampleHitchLogRecords()
) = HitchLogState(
    logId = log.id,
    logName = log.name,
    teamId = log.teamId,
    team = log.team,
    comment = log.comment,
    records = records,
    summary = run {
        val restUsedMin = computeRestMinutes(records)
        val restUsedDivisions = computeRestDivisions(records)
        val restLeftMin = computeRestLeft(records, totalRestMin = null)
        val restLeftDivisions = computeRestDivisionsLeft(records, totalRestDivisions = null)
        
        SummaryCardState(
            lifts = records.count { it.type == HitchLogRecordType.LIFT },
            checkpoints = records.count { it.type == HitchLogRecordType.CHECKPOINT },
            restUsedDisplay = "${formatMinutes(restUsedMin)}/$restUsedDivisions",
            restLeftDisplay = "${formatMinutes(restLeftMin)}/$restLeftDivisions",
            showUsed = true,
            liveState = computeLiveState(records)
        )
    },
    ladder = nextActionLadder(records)
)
