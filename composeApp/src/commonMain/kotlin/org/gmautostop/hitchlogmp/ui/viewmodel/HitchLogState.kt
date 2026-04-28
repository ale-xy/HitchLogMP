package org.gmautostop.hitchlogmp.ui.viewmodel

import org.gmautostop.hitchlogmp.domain.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.LiveState

data class SummaryCardState(
    val lifts: Int,
    val checkpoints: Int,
    val restMin: Int,
    val liveState: LiveState?
)

data class HitchLogState(
    val logName: String,
    val teamId: String,
    val records: List<HitchLogRecord>,
    val summary: SummaryCardState,
    val ladder: List<HitchLogRecordType>
)
