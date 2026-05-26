package org.gmautostop.hitchlogmp

import kotlinx.datetime.LocalTime

/**
 * Parses time input in flexible formats and returns a LocalTime if valid.
 * 
 * Supported formats:
 * - HH:mm or H:mm (colon separator, e.g., "14:30", "9:30")
 * - HH.mm or H.mm (dot separator, e.g., "14.30", "9.30")
 * - HHmm (exactly 4 digits, no separator, e.g., "1430", "0930")
 * 
 * Invalid formats:
 * - 3 digits or less without delimiters (e.g., "930", "14", "5")
 * - Hour > 23 or minute > 59
 * - Non-numeric input
 * 
 * @param input The time string to parse
 * @return LocalTime if parsing succeeds and time is valid, null otherwise
 */
fun parseFlexibleTime(input: String): LocalTime? {
    val trimmed = input.trim()
    
    if (trimmed.isEmpty()) {
        return null
    }
    
    // 1. Try HH:mm or H:mm (colon separator)
    val colonMatch = """^(\d{1,2}):(\d{2})$""".toRegex().matchEntire(trimmed)
    if (colonMatch != null) {
        val hour = colonMatch.groupValues[1].toIntOrNull() ?: return null
        val minute = colonMatch.groupValues[2].toIntOrNull() ?: return null
        if (hour in 0..23 && minute in 0..59) {
            return LocalTime(hour, minute)
        }
        return null
    }
    
    // 2. Try HH.mm or H.mm (dot separator)
    val dotMatch = """^(\d{1,2})\.(\d{2})$""".toRegex().matchEntire(trimmed)
    if (dotMatch != null) {
        val hour = dotMatch.groupValues[1].toIntOrNull() ?: return null
        val minute = dotMatch.groupValues[2].toIntOrNull() ?: return null
        if (hour in 0..23 && minute in 0..59) {
            return LocalTime(hour, minute)
        }
        return null
    }
    
    // 3. Try HHmm (exactly 4 digits, no separator)
    val fourDigitMatch = """^(\d{4})$""".toRegex().matchEntire(trimmed)
    if (fourDigitMatch != null) {
        val value = fourDigitMatch.groupValues[1]
        val hour = value.substring(0, 2).toIntOrNull() ?: return null
        val minute = value.substring(2, 4).toIntOrNull() ?: return null
        if (hour in 0..23 && minute in 0..59) {
            return LocalTime(hour, minute)
        }
        return null
    }
    
    // All patterns failed
    return null
}
