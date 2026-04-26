package org.gmautostop.hitchlogmp.ui.viewmodel

import org.gmautostop.hitchlogmp.domain.HitchLog
import org.gmautostop.hitchlogmp.domain.HitchLogRecord

data class HitchLogState(
    val log: HitchLog,
    val records: List<HitchLogRecord>
)
