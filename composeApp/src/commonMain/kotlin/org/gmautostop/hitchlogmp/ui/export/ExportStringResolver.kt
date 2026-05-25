package org.gmautostop.hitchlogmp.ui.export

import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.change_type_create
import hitchlogmp.composeapp.generated.resources.change_type_delete
import hitchlogmp.composeapp.generated.resources.change_type_update
import hitchlogmp.composeapp.generated.resources.checkpoint
import hitchlogmp.composeapp.generated.resources.checkpoints
import hitchlogmp.composeapp.generated.resources.chronicle
import hitchlogmp.composeapp.generated.resources.finish
import hitchlogmp.composeapp.generated.resources.free_text
import hitchlogmp.composeapp.generated.resources.get_off
import hitchlogmp.composeapp.generated.resources.history_column_action
import hitchlogmp.composeapp.generated.resources.history_column_changes
import hitchlogmp.composeapp.generated.resources.history_column_edit_datetime
import hitchlogmp.composeapp.generated.resources.history_column_edit_time
import hitchlogmp.composeapp.generated.resources.history_column_record
import hitchlogmp.composeapp.generated.resources.history_record_created
import hitchlogmp.composeapp.generated.resources.history_record_deleted
import hitchlogmp.composeapp.generated.resources.lift
import hitchlogmp.composeapp.generated.resources.lifts
import hitchlogmp.composeapp.generated.resources.meet
import hitchlogmp.composeapp.generated.resources.note
import hitchlogmp.composeapp.generated.resources.offside_off
import hitchlogmp.composeapp.generated.resources.offside_on
import hitchlogmp.composeapp.generated.resources.parameter
import hitchlogmp.composeapp.generated.resources.records
import hitchlogmp.composeapp.generated.resources.rest_off
import hitchlogmp.composeapp.generated.resources.rest_on
import hitchlogmp.composeapp.generated.resources.rest_used_full
import hitchlogmp.composeapp.generated.resources.retire
import hitchlogmp.composeapp.generated.resources.sort_by_record
import hitchlogmp.composeapp.generated.resources.sort_by_time
import hitchlogmp.composeapp.generated.resources.start
import hitchlogmp.composeapp.generated.resources.summary
import hitchlogmp.composeapp.generated.resources.text
import hitchlogmp.composeapp.generated.resources.time
import hitchlogmp.composeapp.generated.resources.type
import hitchlogmp.composeapp.generated.resources.value
import hitchlogmp.composeapp.generated.resources.walk
import hitchlogmp.composeapp.generated.resources.walk_end
import org.gmautostop.hitchlogmp.domain.model.ChangeType
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.jetbrains.compose.resources.getString

/**
 * Pre-resolves all export strings into a context object.
 * Call this once per export operation to avoid duplicate suspend calls.
 */
suspend fun buildExportStrings(): ExportStrings {
    return ExportStrings(
        chronicle = getString(Res.string.chronicle),
        timeLabel = getString(Res.string.time),
        typeLabel = getString(Res.string.type),
        textLabel = getString(Res.string.text),
        noteLabel = getString(Res.string.note),
        summary = getString(Res.string.summary),
        parameterLabel = getString(Res.string.parameter),
        valueLabel = getString(Res.string.value),
        recordsLabel = getString(Res.string.records),
        liftsLabel = getString(Res.string.lifts),
        checkpointsLabel = getString(Res.string.checkpoints),
        restUsedFull = getString(Res.string.rest_used_full),
        sortByRecord = getString(Res.string.sort_by_record),
        sortByTime = getString(Res.string.sort_by_time),
        columnAction = getString(Res.string.history_column_action),
        columnEditDateTime = getString(Res.string.history_column_edit_datetime),
        columnEditTime = getString(Res.string.history_column_edit_time),
        columnChanges = getString(Res.string.history_column_changes),
        columnRecord = getString(Res.string.history_column_record),
        recordDeleted = getString(Res.string.history_record_deleted),
        recordCreated = getString(Res.string.history_record_created),
        recordTypeLabels = HitchLogRecordType.entries.associate { type ->
            type to when (type) {
                HitchLogRecordType.START -> getString(Res.string.start)
                HitchLogRecordType.LIFT -> getString(Res.string.lift)
                HitchLogRecordType.GET_OFF -> getString(Res.string.get_off)
                HitchLogRecordType.WALK -> getString(Res.string.walk)
                HitchLogRecordType.WALK_END -> getString(Res.string.walk_end)
                HitchLogRecordType.CHECKPOINT -> getString(Res.string.checkpoint)
                HitchLogRecordType.MEET -> getString(Res.string.meet)
                HitchLogRecordType.REST_ON -> getString(Res.string.rest_on)
                HitchLogRecordType.REST_OFF -> getString(Res.string.rest_off)
                HitchLogRecordType.OFFSIDE_ON -> getString(Res.string.offside_on)
                HitchLogRecordType.OFFSIDE_OFF -> getString(Res.string.offside_off)
                HitchLogRecordType.FINISH -> getString(Res.string.finish)
                HitchLogRecordType.RETIRE -> getString(Res.string.retire)
                HitchLogRecordType.FREE_TEXT -> getString(Res.string.free_text)
            }
        },
        changeTypeLabels = ChangeType.entries.associate { type ->
            type to when (type) {
                ChangeType.CREATE -> getString(Res.string.change_type_create)
                ChangeType.UPDATE -> getString(Res.string.change_type_update)
                ChangeType.DELETE -> getString(Res.string.change_type_delete)
            }
        }
    )
}
