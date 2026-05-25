package org.gmautostop.hitchlogmp.ui.hitchlog

import org.gmautostop.hitchlogmp.domain.model.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.logic.LiveState

data class SummaryCardState(
    val lifts: Int,
    val checkpoints: Int,
    val restUsedDisplay: String,
    val restLeftDisplay: String,
    val showUsed: Boolean = true,
    val liveState: LiveState?
)

data class HitchLogState(
    val logId: String,
    val logName: String,
    val teamId: String,
    val records: List<HitchLogRecord>,
    val summary: SummaryCardState,
    val ladder: List<HitchLogRecordType>
)
