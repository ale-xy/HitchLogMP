package org.gmautostop.hitchlogmp.domain

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.gmautostop.hitchlogmp.localTZDateTime
import kotlin.time.Clock

@Serializable
data class HitchLog(
    var id: String = "",
    val userId: String = "",
    val raceId: String = "",
    val teamId: String = "",
    val name: String = "",
    val creationTime: Timestamp = Timestamp.now()
)

data class HitchLogRecord(
    val id: String = "",
    val time: LocalDateTime = Clock.System.now().localTZDateTime(),
    val type: HitchLogRecordType = HitchLogRecordType.FREE_TEXT,
    val text: String = "",
    val deleted: Boolean = false,
    val edited: Boolean = false
)

@Serializable
enum class HitchLogRecordType {
    START, LIFT, GET_OFF, WALK, WALK_END,
    CHECKPOINT, MEET, REST_ON, REST_OFF,
    OFFSIDE_ON, OFFSIDE_OFF, FINISH, RETIRE, FREE_TEXT,
}

enum class ChangeType {
    CREATE, UPDATE, DELETE
}

data class HitchLogRecordHistoryEntry(
    val historyId: String,
    val editedAt: Instant,
    val changeType: ChangeType,
    val time: LocalDateTime,
    val type: HitchLogRecordType,
    val text: String
)

//class GeoPointRecord(id: String, val point: GeoPoint = GeoPoint(0.0, 0.0), time: Date, type: HitchLogRecordType, text: String)
//    : HitchLogRecord(id, time, type, text)

