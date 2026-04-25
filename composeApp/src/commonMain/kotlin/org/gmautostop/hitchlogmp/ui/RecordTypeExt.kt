package org.gmautostop.hitchlogmp.ui

import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.checkpoint
import hitchlogmp.composeapp.generated.resources.finish
import hitchlogmp.composeapp.generated.resources.free_text
import hitchlogmp.composeapp.generated.resources.get_off
import hitchlogmp.composeapp.generated.resources.lift
import hitchlogmp.composeapp.generated.resources.meet
import hitchlogmp.composeapp.generated.resources.offside_off
import hitchlogmp.composeapp.generated.resources.offside_on
import hitchlogmp.composeapp.generated.resources.rest_off
import hitchlogmp.composeapp.generated.resources.rest_on
import hitchlogmp.composeapp.generated.resources.retire
import hitchlogmp.composeapp.generated.resources.start
import hitchlogmp.composeapp.generated.resources.walk
import hitchlogmp.composeapp.generated.resources.walk_end
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.jetbrains.compose.resources.StringResource

fun HitchLogRecordType.toStringResource(): StringResource = when (this) {
    HitchLogRecordType.START       -> Res.string.start
    HitchLogRecordType.LIFT        -> Res.string.lift
    HitchLogRecordType.GET_OFF     -> Res.string.get_off
    HitchLogRecordType.WALK        -> Res.string.walk
    HitchLogRecordType.WALK_END    -> Res.string.walk_end
    HitchLogRecordType.CHECKPOINT  -> Res.string.checkpoint
    HitchLogRecordType.MEET        -> Res.string.meet
    HitchLogRecordType.REST_ON     -> Res.string.rest_on
    HitchLogRecordType.REST_OFF    -> Res.string.rest_off
    HitchLogRecordType.OFFSIDE_ON  -> Res.string.offside_on
    HitchLogRecordType.OFFSIDE_OFF -> Res.string.offside_off
    HitchLogRecordType.FINISH      -> Res.string.finish
    HitchLogRecordType.RETIRE      -> Res.string.retire
    HitchLogRecordType.FREE_TEXT   -> Res.string.free_text
}
