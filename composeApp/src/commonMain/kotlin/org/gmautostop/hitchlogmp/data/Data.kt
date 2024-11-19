package org.gmautostop.hitchlogmp.data

import dev.gitlive.firebase.firestore.DoubleAsTimestampSerializer
import dev.gitlive.firebase.firestore.Timestamp
import dev.gitlive.firebase.firestore.TimestampSerializer
import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.gmautostop.hitchlogmp.localTZDateTime
import org.gmautostop.hitchlogmp.toLocalDateTime
import org.jetbrains.compose.resources.StringResource

@Serializable
data class HitchLog(
    var id: String = "",
    val userId: String = "",
    val raceId: String = "",
    val teamId: String = "",
    val name: String = "",
//    val creationTime: LocalDateTime = Clock.System.now().localTZDateTime(),
    val creationTime: Timestamp = Timestamp.now()
)

@Serializable
data class HitchLogRecord(
    val id: String = "",
//    val time: LocalDateTime = Clock.System.now().localTZDateTime(),
    val time: Timestamp = Timestamp.now(),
    val type: HitchLogRecordType = HitchLogRecordType.FREE_TEXT,
    val text: String = ""
)

fun HitchLogRecord.getTime() = time.toLocalDateTime()

@Serializable
enum class HitchLogRecordType(val text: StringResource) {
    START(Res.string.start),
    LIFT(Res.string.lift),
    GET_OFF(Res.string.get_off),
    WALK(Res.string.walk),
    WALK_END(Res.string.walk_end),
    CHECKPOINT(Res.string.checkpoint),
    MEET(Res.string.meet),
    REST_ON(Res.string.rest_on),
    REST_OFF(Res.string.rest_off),
    OFFSIDE_ON(Res.string.offside_on),
    OFFSIDE_OFF(Res.string.offside_off),
    FINISH(Res.string.finish),
    RETIRE(Res.string.retire),
    FREE_TEXT(Res.string.free_text),

}

//class GeoPointRecord(id: String, val point: GeoPoint = GeoPoint(0.0, 0.0), time: Date, type: HitchLogRecordType, text: String)
//    : HitchLogRecord(id, time, type, text)

