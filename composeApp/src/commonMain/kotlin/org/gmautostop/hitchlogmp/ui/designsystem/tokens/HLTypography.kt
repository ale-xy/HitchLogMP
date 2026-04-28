package org.gmautostop.hitchlogmp.ui.designsystem.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography scale for HitchLog application.
 * Extracted from HitchLogScreen design patterns.
 */
object HLTypography {
    // Title styles - used for screen titles and section headers
    val titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium
    )

    val titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )

    // Body styles - used for main content text
    val bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    )

    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal
    )

    // Label styles - used for buttons, badges, and UI labels
    val labelLarge = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )

    val labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
    )

    val labelSmall = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.Normal
    )

    // Specialized styles
    val statValue = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium
    )

    val subtitle = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    )
}
