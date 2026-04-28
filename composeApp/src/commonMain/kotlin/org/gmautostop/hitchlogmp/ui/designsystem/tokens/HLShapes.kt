package org.gmautostop.hitchlogmp.ui.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Shape definitions for HitchLog application.
 * Provides consistent corner radii across all components.
 */
object HLShapes {
    val small = RoundedCornerShape(8.dp)           // Small elements, chips
    val medium = RoundedCornerShape(12.dp)         // Cards, buttons
    val large = RoundedCornerShape(16.dp)          // Large cards, sheet items
    val extraLarge = RoundedCornerShape(20.dp)     // Prominent buttons
    val bottomSheet = RoundedCornerShape(          // Bottom sheets
        topStart = 28.dp,
        topEnd = 28.dp
    )
    val pill = RoundedCornerShape(100.dp)          // Fully rounded (badges, FABs)
}
