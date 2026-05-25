package org.gmautostop.hitchlogmp.ui.export

import org.gmautostop.hitchlogmp.domain.model.ChangeType
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType

/**
 * Pre-resolved strings for export formatters.
 * Eliminates duplicate suspend calls to resolveRecordTypeLabel, resolveChangeTypeLabel, etc.
 */
class ExportStrings(
    // Chronicle table headers
    val chronicle: String,
    val timeLabel: String,
    val typeLabel: String,
    val textLabel: String,
    val noteLabel: String,
    
    // Summary table headers and labels
    val summary: String,
    val parameterLabel: String,
    val valueLabel: String,
    val recordsLabel: String,
    val liftsLabel: String,
    val checkpointsLabel: String,
    val restUsedFull: String,
    
    // History section
    val sortByRecord: String,
    val sortByTime: String,
    val columnAction: String,
    val columnEditDateTime: String,
    val columnEditTime: String,
    val columnChanges: String,
    val columnRecord: String,
    val recordDeleted: String,
    val recordCreated: String,
    
    // Enum-based lookups (Maps)
    val recordTypeLabels: Map<HitchLogRecordType, String>,
    val changeTypeLabels: Map<ChangeType, String>
)
