package org.gmautostop.hitchlogmp.ui.designsystem.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography scale for HitchLog application.
 * Extracted from HitchLogScreen design patterns.
 */
object HLTypography {
    // Display styles - used for large prominent text
    val displaySmall = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.5).sp
    )

    // Title styles - used for screen titles and section headers
    val titleLarge = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp
    )

    val titleMedium = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )

    // Body styles - used for main content text
    val bodyLarge = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    )

    val bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    val bodySmall = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )

    // Label styles - used for buttons, badges, and UI labels
    val labelLarge = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp
    )

    val labelMedium = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
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
