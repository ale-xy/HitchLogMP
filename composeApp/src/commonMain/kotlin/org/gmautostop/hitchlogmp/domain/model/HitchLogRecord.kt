package org.gmautostop.hitchlogmp.domain.model

import kotlinx.datetime.LocalDateTime
import org.gmautostop.hitchlogmp.localTZDateTime
import kotlin.time.Clock

data class HitchLogRecord(
    val id: String = "",
    val time: LocalDateTime = Clock.System.now().localTZDateTime(),
    val type: HitchLogRecordType = HitchLogRecordType.FREE_TEXT,
    val text: String = "",
    val deleted: Boolean = false,
    val edited: Boolean = false
)
