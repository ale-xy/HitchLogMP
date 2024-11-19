package org.gmautostop.hitchlogmp.data

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.toLocalDateTime
import org.gmautostop.hitchlogmp.toTimestamp

@Serializable
data class FirestoreHitchLogRecord(
    val id: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: HitchLogRecordType = HitchLogRecordType.FREE_TEXT,
    val text: String = ""
)  {
    constructor(id: String, time: LocalDateTime, type: HitchLogRecordType, text: String) : this(
        id, time.toTimestamp(), type, text
    )

    constructor(
        from: HitchLogRecord,
        id: String = from.id,
        timestamp: Timestamp = from.time.toTimestamp()
    ) : this(
        id = id,
        timestamp = timestamp,
        type = from.type,
        text = from.text
    )

    fun toHitchLogRecord() = HitchLogRecord(id, timestamp.toLocalDateTime(), type, text)
}