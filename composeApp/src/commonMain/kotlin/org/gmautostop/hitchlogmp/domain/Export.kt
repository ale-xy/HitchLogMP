package org.gmautostop.hitchlogmp.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.html.body
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.html
import kotlinx.html.meta
import kotlinx.html.stream.appendHTML
import kotlinx.html.style
import kotlinx.html.table
import kotlinx.html.tbody
import kotlinx.html.td
import kotlinx.html.th
import kotlinx.html.thead
import kotlinx.html.title
import kotlinx.html.tr
import kotlinx.html.unsafe
import org.gmautostop.hitchlogmp.dateFormat
import org.gmautostop.hitchlogmp.formatDateLocale
import org.gmautostop.hitchlogmp.timeFormatForDisplay

object MimeTypes {
    const val TEXT_PLAIN = "text/plain"
    const val TEXT_CSV = "text/csv"
    const val TEXT_HTML = "text/html"
}

private val moscowTZ = TimeZone.of("Europe/Moscow")

private fun toMoscow(dt: LocalDateTime): LocalDateTime =
    dt.toInstant(TimeZone.currentSystemDefault()).toLocalDateTime(moscowTZ)

private val typeLabels = mapOf(
    HitchLogRecordType.START to "Старт",
    HitchLogRecordType.LIFT to "Посадка",
    HitchLogRecordType.GET_OFF to "Выход",
    HitchLogRecordType.WALK to "ППХ",
    HitchLogRecordType.WALK_END to "Конец ППХ",
    HitchLogRecordType.CHECKPOINT to "КП",
    HitchLogRecordType.MEET to "Встреча",
    HitchLogRecordType.REST_ON to "Rest on",
    HitchLogRecordType.REST_OFF to "Rest off",
    HitchLogRecordType.OFFSIDE_ON to "Вне игры",
    HitchLogRecordType.OFFSIDE_OFF to "Вне игры off",
    HitchLogRecordType.FINISH to "Финиш",
    HitchLogRecordType.RETIRE to "Сход",
    HitchLogRecordType.FREE_TEXT to "Прочее",
)

fun formatAsTxt(log: HitchLog, records: List<HitchLogRecord>): String = buildString {
    append(log.name)
    append("\n\n")

    val recordsByDate = records
        .map { it.copy(time = toMoscow(it.time)) }
        .sortedBy { it.time }
        .groupBy { it.time.date }

    for ((date, dayRecords) in recordsByDate) {
        append(formatDateLocale(date))
        append("\n\n")

        for (record in dayRecords) {
            val timeStr = timeFormatForDisplay.format(record.time)
            val typeLabel = typeLabels[record.type] ?: record.type.name
            append(timeStr)
            append(" ")
            append(typeLabel)
            if (record.text.isNotBlank()) {
                append(" — ")
                append(record.text)
            }
            append("\n")
        }
        append("\n")
    }

    // Stats
    append("——————\n")
    val lifts = records.count { it.type == HitchLogRecordType.LIFT }
    val checkpoints = records.count { it.type == HitchLogRecordType.CHECKPOINT }
    val restMin = computeRestMinutes(records)

    append("Машин: $lifts\n")
    append("КП: $checkpoints\n")
    append("Rest: ${formatMinutes(restMin)} использовано\n")
}

fun formatAsCsv(log: HitchLog, records: List<HitchLogRecord>): String = buildString {
    // Don't add BOM here - it will be added as bytes on Android
    append("date,time,type,note\n")

    val sorted = records
        .map { it.copy(time = toMoscow(it.time)) }
        .sortedBy { it.time }

    for (record in sorted) {
        val dateStr = dateFormat.format(record.time.date)
        val timeStr = timeFormatForDisplay.format(record.time)
        val typeStr = record.type.name
        val noteStr = escapeCsv(record.text)

        append(dateStr)
        append(",")
        append(timeStr)
        append(",")
        append(typeStr)
        append(",")
        append(noteStr)
        append("\n")
    }
}

private fun escapeCsv(value: String): String {
    if (value.isEmpty()) return value
    if (value.contains(',') || value.contains('"') || value.contains('\n')) {
        return "\"${value.replace("\"", "\"\"")}\""
    }
    return value
}

fun formatAsHtml(log: HitchLog, records: List<HitchLogRecord>): String {
    val lifts = records.count { it.type == HitchLogRecordType.LIFT }
    val checkpoints = records.count { it.type == HitchLogRecordType.CHECKPOINT }
    val restMin = computeRestMinutes(records)

    val sorted = records
        .map { it.copy(time = toMoscow(it.time)) }
        .sortedBy { it.time }

    val recordsByDate = sorted.groupBy { it.time.date }

    return buildString {
        appendHTML().html {
            head {
                meta(charset = "UTF-8")
                title(log.name)
                style {
                    unsafe {
                        +"""
                            body { font-family: Arial, sans-serif; margin: 20px; }
                            table { border-collapse: collapse; width: 100%; margin-bottom: 20px; }
                            th, td { border: 1px solid #ccc; padding: 8px; text-align: left; }
                            th { background-color: #f0f0f0; font-weight: bold; }
                            .summary-table { max-width: 500px; }
                            .date-header { background-color: #e0e0e0; font-weight: bold; }
                        """.trimIndent()
                    }
                }
            }
            body {
                h1 { text(log.name) }
                
                h2 { text("Хроника") }
                table {
                    thead {
                        tr {
                            th { text("Время") }
                            th { text("Тип") }
                            th { text("Заметка") }
                        }
                    }
                    tbody {
                        for ((date, dayRecords) in recordsByDate) {
                            val dateStr = formatDateLocale(date)
                            tr("date-header") {
                                td {
                                    colSpan = "3"
                                    text(dateStr)
                                }
                            }
                            for (record in dayRecords) {
                                val timeStr = timeFormatForDisplay.format(record.time)
                                val typeLabel = typeLabels[record.type] ?: record.type.name
                                val color = getRowColor(record.type)
                                tr {
                                    style = "background-color: $color;"
                                    td { text(timeStr) }
                                    td { text(typeLabel) }
                                    td { text(record.text) }
                                }
                            }
                        }
                    }
                }
                
                h2 { text("Сводка") }
                table("summary-table") {
                    tr {
                        th { text("Параметр") }
                        th { text("Значение") }
                    }
                    tr {
                        td { text("Записей") }
                        td { text(records.size.toString()) }
                    }
                    tr {
                        td { text("Машин") }
                        td { text(lifts.toString()) }
                    }
                    tr {
                        td { text("КП") }
                        td { text(checkpoints.toString()) }
                    }
                    tr {
                        td { text("Rest использовано") }
                        td { text(formatMinutes(restMin)) }
                    }
                }
            }
        }
    }
}

private fun getRowColor(type: HitchLogRecordType): String = when (type) {
    HitchLogRecordType.START, HitchLogRecordType.FINISH -> "#FCE4EC"
    HitchLogRecordType.LIFT -> "#E3F2FD"
    HitchLogRecordType.GET_OFF -> "#F3E5F5"
    HitchLogRecordType.WALK, HitchLogRecordType.WALK_END -> "#FFF8E1"
    HitchLogRecordType.CHECKPOINT -> "#E8F5E9"
    HitchLogRecordType.REST_ON, HitchLogRecordType.REST_OFF -> "#FBE9E7"
    HitchLogRecordType.OFFSIDE_ON, HitchLogRecordType.OFFSIDE_OFF -> "#ECEFF1"
    HitchLogRecordType.RETIRE -> "#FFEBEE"
    HitchLogRecordType.MEET, HitchLogRecordType.FREE_TEXT -> "#FFFFFF"
}


