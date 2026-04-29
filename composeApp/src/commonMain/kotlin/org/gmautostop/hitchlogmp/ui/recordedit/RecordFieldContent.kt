package org.gmautostop.hitchlogmp.ui.recordedit

import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.field_label_car_brand
import hitchlogmp.composeapp.generated.resources.field_label_checkpoint_number
import hitchlogmp.composeapp.generated.resources.field_label_dropoff_location
import hitchlogmp.composeapp.generated.resources.field_label_location
import hitchlogmp.composeapp.generated.resources.field_label_note
import hitchlogmp.composeapp.generated.resources.field_label_offside_reason
import hitchlogmp.composeapp.generated.resources.field_label_participant_number
import hitchlogmp.composeapp.generated.resources.field_label_walk_destination
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.jetbrains.compose.resources.StringResource

/**
 * Returns the appropriate field label for a given record type.
 * Used to customize the text field label based on what information is expected.
 */
fun recordFieldLabel(type: HitchLogRecordType): StringResource = when (type) {
    HitchLogRecordType.START -> Res.string.field_label_note
    HitchLogRecordType.LIFT -> Res.string.field_label_car_brand
    HitchLogRecordType.GET_OFF -> Res.string.field_label_dropoff_location
    HitchLogRecordType.WALK -> Res.string.field_label_walk_destination
    HitchLogRecordType.WALK_END -> Res.string.field_label_location
    HitchLogRecordType.CHECKPOINT -> Res.string.field_label_checkpoint_number
    HitchLogRecordType.MEET -> Res.string.field_label_participant_number
    HitchLogRecordType.REST_ON -> Res.string.field_label_note
    HitchLogRecordType.REST_OFF -> Res.string.field_label_note
    HitchLogRecordType.OFFSIDE_ON -> Res.string.field_label_offside_reason
    HitchLogRecordType.OFFSIDE_OFF -> Res.string.field_label_note
    HitchLogRecordType.FINISH -> Res.string.field_label_checkpoint_number
    HitchLogRecordType.RETIRE -> Res.string.field_label_offside_reason
    HitchLogRecordType.FREE_TEXT -> Res.string.field_label_note
}
