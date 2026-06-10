package org.gmautostop.hitchlogmp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.ui.designsystem.tokens.RecordColor

data class RecordTypeUi(val icon: ImageVector, val color: RecordColor, val end: Boolean = false)

fun HitchLogRecordType.toUi(): RecordTypeUi = when (this) {
    HitchLogRecordType.START -> RecordTypeUi(Icons.Filled.Timer, RecordColor.BLUE)
    HitchLogRecordType.LIFT -> RecordTypeUi(Icons.Filled.DirectionsCar, RecordColor.YELLOW)
    HitchLogRecordType.GET_OFF -> RecordTypeUi(Icons.AutoMirrored.Filled.Logout, RecordColor.YELLOW, end = true)
    HitchLogRecordType.WALK -> RecordTypeUi(Icons.AutoMirrored.Filled.DirectionsWalk, RecordColor.ORANGE)
    HitchLogRecordType.WALK_END -> RecordTypeUi(Icons.Filled.AccessibilityNew, RecordColor.ORANGE, end = true)
    HitchLogRecordType.CHECKPOINT -> RecordTypeUi(Icons.Filled.LocationOn, RecordColor.BLUE)
    HitchLogRecordType.MEET -> RecordTypeUi(Icons.Filled.Group, RecordColor.NEUTRAL_DARK)
    HitchLogRecordType.REST_ON -> RecordTypeUi(Icons.Filled.Hotel, RecordColor.GREEN)
    HitchLogRecordType.REST_OFF -> RecordTypeUi(Icons.Filled.LightMode, RecordColor.GREEN, end = true)
    HitchLogRecordType.OFFSIDE_ON -> RecordTypeUi(Icons.Filled.PauseCircle, RecordColor.MAGENTA)
    HitchLogRecordType.OFFSIDE_OFF -> RecordTypeUi(Icons.Filled.PlayArrow, RecordColor.MAGENTA, end = true)
    HitchLogRecordType.FINISH -> RecordTypeUi(Icons.Filled.SportsScore, RecordColor.BLUE)
    HitchLogRecordType.RETIRE -> RecordTypeUi(Icons.Filled.Block, RecordColor.PURPLE)
    HitchLogRecordType.FREE_TEXT -> RecordTypeUi(Icons.AutoMirrored.Filled.StickyNote2, RecordColor.NEUTRAL_LIGHT)
}

fun recordTypeIcon(type: HitchLogRecordType): ImageVector = type.toUi().icon
