package org.gmautostop.hitchlogmp.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

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
