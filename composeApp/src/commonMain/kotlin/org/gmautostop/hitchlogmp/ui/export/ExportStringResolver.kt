package org.gmautostop.hitchlogmp.ui.export

import hitchlogmp.composeapp.generated.resources.Res
import hitchlogmp.composeapp.generated.resources.change_type_create
import hitchlogmp.composeapp.generated.resources.change_type_delete
import hitchlogmp.composeapp.generated.resources.change_type_update
import hitchlogmp.composeapp.generated.resources.checkpoints
import hitchlogmp.composeapp.generated.resources.chronicle
import hitchlogmp.composeapp.generated.resources.date
import hitchlogmp.composeapp.generated.resources.history_column_action
import hitchlogmp.composeapp.generated.resources.history_column_after
import hitchlogmp.composeapp.generated.resources.history_column_before
import hitchlogmp.composeapp.generated.resources.history_column_changes
import hitchlogmp.composeapp.generated.resources.history_column_edit_datetime
import hitchlogmp.composeapp.generated.resources.history_column_edit_time
import hitchlogmp.composeapp.generated.resources.history_column_record
import hitchlogmp.composeapp.generated.resources.history_record_created
import hitchlogmp.composeapp.generated.resources.history_record_deleted
import hitchlogmp.composeapp.generated.resources.lifts
import hitchlogmp.composeapp.generated.resources.note
import hitchlogmp.composeapp.generated.resources.records
import hitchlogmp.composeapp.generated.resources.rest_used_full
import hitchlogmp.composeapp.generated.resources.sort_by_record
import hitchlogmp.composeapp.generated.resources.sort_by_time
import hitchlogmp.composeapp.generated.resources.summary
import hitchlogmp.composeapp.generated.resources.text
import hitchlogmp.composeapp.generated.resources.time
import hitchlogmp.composeapp.generated.resources.type
import org.gmautostop.hitchlogmp.domain.model.ChangeType
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.components.toStringResource
import org.jetbrains.compose.resources.getString

/**
 * Pre-resolves all export strings into a context object.
 * Call this once per export operation to avoid duplicate suspend calls.
 */
suspend fun buildExportStrings(): ExportStrings {
    return ExportStrings(
        chronicle = getString(Res.string.chronicle),
        dateLabel = getString(Res.string.date),
        timeLabel = getString(Res.string.time),
        typeLabel = getString(Res.string.type),
        textLabel = getString(Res.string.text),
        noteLabel = getString(Res.string.note),
        summary = getString(Res.string.summary),
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
        columnBefore = getString(Res.string.history_column_before),
        columnAfter = getString(Res.string.history_column_after),
        columnRecord = getString(Res.string.history_column_record),
        recordDeleted = getString(Res.string.history_record_deleted),
        recordCreated = getString(Res.string.history_record_created),
        recordTypeLabels = HitchLogRecordType.entries.associateWith { type ->
            getString(type.toStringResource())
        },
        changeTypeLabels = ChangeType.entries.associateWith { type ->
            when (type) {
                ChangeType.CREATE -> getString(Res.string.change_type_create)
                ChangeType.UPDATE -> getString(Res.string.change_type_update)
                ChangeType.DELETE -> getString(Res.string.change_type_delete)
            }
        }
    )
}
