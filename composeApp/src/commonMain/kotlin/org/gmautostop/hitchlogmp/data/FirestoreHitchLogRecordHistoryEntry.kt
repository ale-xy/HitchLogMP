package org.gmautostop.hitchlogmp.data

import dev.gitlive.firebase.firestore.Timestamp
import kotlinx.serialization.Serializable
import org.gmautostop.hitchlogmp.domain.model.ChangeType
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordHistoryEntry
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.toInstant
import org.gmautostop.hitchlogmp.toLocalDateTime
import org.gmautostop.hitchlogmp.toTimestamp

@Serializable
data class FirestoreHitchLogRecordHistoryEntry(
    val historyId: String = "",
    val editedAt: Timestamp = Timestamp.now(),
    val changeType: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val type: HitchLogRecordType = HitchLogRecordType.FREE_TEXT,
    val text: String = ""
) {
    constructor(
        from: HitchLogRecordHistoryEntry
    ) : this(
        historyId = from.historyId,
        editedAt = from.editedAt.toTimestamp(),
        changeType = from.changeType.name,
        timestamp = from.time.toTimestamp(),
        type = from.type,
        text = from.text
    )

    fun toHitchLogRecordHistoryEntry() = HitchLogRecordHistoryEntry(
        historyId = historyId,
        editedAt = editedAt.toInstant(),
        changeType = ChangeType.valueOf(changeType),
        time = timestamp.toLocalDateTime(),
        type = type,
        text = text
    )
}
