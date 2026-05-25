package org.gmautostop.hitchlogmp.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class HitchLogRecordType {
    START, LIFT, GET_OFF, WALK, WALK_END,
    CHECKPOINT, MEET, REST_ON, REST_OFF,
    OFFSIDE_ON, OFFSIDE_OFF, FINISH, RETIRE, FREE_TEXT,
}
