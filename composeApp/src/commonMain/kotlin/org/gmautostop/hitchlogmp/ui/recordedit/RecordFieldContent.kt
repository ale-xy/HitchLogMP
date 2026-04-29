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
import hitchlogmp.composeapp.generated.resources.placeholder_car_example
import hitchlogmp.composeapp.generated.resources.placeholder_checkpoint_example
import hitchlogmp.composeapp.generated.resources.placeholder_dropoff_example
import hitchlogmp.composeapp.generated.resources.placeholder_finish_example
import hitchlogmp.composeapp.generated.resources.placeholder_free_text
import hitchlogmp.composeapp.generated.resources.placeholder_offside_example
import hitchlogmp.composeapp.generated.resources.placeholder_optional
import hitchlogmp.composeapp.generated.resources.placeholder_participant_example
import hitchlogmp.composeapp.generated.resources.placeholder_retire_example
import hitchlogmp.composeapp.generated.resources.placeholder_walk_example
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

/**
 * Returns the appropriate placeholder text for a given record type.
 * Provides contextual examples to guide user input.
 */
fun recordFieldPlaceholder(type: HitchLogRecordType): StringResource = when (type) {
    HitchLogRecordType.START -> Res.string.placeholder_optional
    HitchLogRecordType.LIFT -> Res.string.placeholder_car_example
    HitchLogRecordType.GET_OFF -> Res.string.placeholder_dropoff_example
    HitchLogRecordType.WALK -> Res.string.placeholder_walk_example
    HitchLogRecordType.WALK_END -> Res.string.placeholder_optional
    HitchLogRecordType.CHECKPOINT -> Res.string.placeholder_checkpoint_example
    HitchLogRecordType.MEET -> Res.string.placeholder_participant_example
    HitchLogRecordType.REST_ON -> Res.string.placeholder_optional
    HitchLogRecordType.REST_OFF -> Res.string.placeholder_optional
    HitchLogRecordType.OFFSIDE_ON -> Res.string.placeholder_offside_example
    HitchLogRecordType.OFFSIDE_OFF -> Res.string.placeholder_optional
    HitchLogRecordType.FINISH -> Res.string.placeholder_finish_example
    HitchLogRecordType.RETIRE -> Res.string.placeholder_retire_example
    HitchLogRecordType.FREE_TEXT -> Res.string.placeholder_free_text
}
