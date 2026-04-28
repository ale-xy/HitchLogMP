package org.gmautostop.hitchlogmp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import org.gmautostop.hitchlogmp.domain.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.ColorRole

data class RecordTypeUi(val icon: ImageVector, val colorRole: ColorRole)

fun HitchLogRecordType.toUi(): RecordTypeUi = when (this) {
    HitchLogRecordType.START -> RecordTypeUi(Icons.Filled.Timer, ColorRole.PRIMARY)
    HitchLogRecordType.LIFT -> RecordTypeUi(Icons.Filled.DirectionsCar, ColorRole.SECONDARY)
    HitchLogRecordType.GET_OFF -> RecordTypeUi(Icons.AutoMirrored.Filled.Logout, ColorRole.SECONDARY)
    HitchLogRecordType.WALK -> RecordTypeUi(Icons.AutoMirrored.Filled.DirectionsWalk, ColorRole.TERTIARY)
    HitchLogRecordType.WALK_END -> RecordTypeUi(Icons.Filled.AccessibilityNew, ColorRole.TERTIARY)
    HitchLogRecordType.CHECKPOINT -> RecordTypeUi(Icons.Filled.LocationOn, ColorRole.PRIMARY)
    HitchLogRecordType.MEET -> RecordTypeUi(Icons.Filled.Group, ColorRole.OUTLINE)
    HitchLogRecordType.REST_ON -> RecordTypeUi(Icons.Filled.Hotel, ColorRole.SURFACE)
    HitchLogRecordType.REST_OFF -> RecordTypeUi(Icons.Filled.PlayArrow, ColorRole.SURFACE)
    HitchLogRecordType.OFFSIDE_ON -> RecordTypeUi(Icons.Filled.PauseCircle, ColorRole.ERROR)
    HitchLogRecordType.OFFSIDE_OFF -> RecordTypeUi(Icons.Filled.PlayArrow, ColorRole.ERROR)
    HitchLogRecordType.FINISH -> RecordTypeUi(Icons.Filled.Flag, ColorRole.PRIMARY)
    HitchLogRecordType.RETIRE -> RecordTypeUi(Icons.Filled.Block, ColorRole.ERROR_BOLD)
    HitchLogRecordType.FREE_TEXT -> RecordTypeUi(Icons.AutoMirrored.Filled.Note, ColorRole.SURFACE)
}
