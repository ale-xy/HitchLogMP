package org.gmautostop.hitchlogmp.domain.logic

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecord
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.CHECKPOINT
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.FINISH
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.FREE_TEXT
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.GET_OFF
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.LIFT
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.MEET
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.REST_OFF
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.REST_ON
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.RETIRE
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.START
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.WALK
import org.gmautostop.hitchlogmp.domain.model.HitchLogRecordType.WALK_END
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RaceLogicTest {

    private val raceDay = LocalDate(2026, 10, 11)
    private val nextDay = LocalDate(2026, 10, 12)

    /** [time] is "HH:mm" or "HH:mm:ss" on [date]. */
    private fun timeAt(time: String, date: LocalDate = raceDay) =
        LocalDateTime(date, LocalTime.parse(time))

    private fun record(time: String, type: HitchLogRecordType, date: LocalDate = raceDay) =
        HitchLogRecord(id = "$date $time $type", time = timeAt(time, date), type = type)

    /** Records as the repository stores them when added one by one in list order (see [storedTimeFor]). */
    private fun withStoredSeconds(records: List<HitchLogRecord>): List<HitchLogRecord> {
        val stored = mutableListOf<HitchLogRecord>()
        for (current in records) {
            stored += current.copy(time = storedTimeFor(current.time, stored.map { it.time }))
        }
        return stored
    }

    // --- minutesBetween ---

    @Test
    fun `given times with seconds when minutesBetween then seconds are ignored`() {
        assertEquals(12, minutesBetween(timeAt("11:20:01"), timeAt("11:32:00")))
        assertEquals(12, minutesBetween(timeAt("11:20:00"), timeAt("11:32:59")))
        assertEquals(0, minutesBetween(timeAt("11:20:00"), timeAt("11:20:59")))
    }

    @Test
    fun `given times across midnight when minutesBetween then minutes are counted across days`() {
        assertEquals(20, minutesBetween(timeAt("23:50"), timeAt("00:10", nextDay)))
    }

    @Test
    fun `given end before start when minutesBetween then zero`() {
        assertEquals(0, minutesBetween(timeAt("11:30"), timeAt("11:20")))
    }

    // --- computeRestMinutes ---

    @Test
    fun `given rest on stored a second later in a shared minute when computeRestMinutes then full minutes are counted`() {
        // The reported bug: Rest on stored at :01 because Высадка shares its minute
        val records = withStoredSeconds(listOf(
            record("11:20", GET_OFF),
            record("11:20", REST_ON),
            record("11:32", REST_OFF),
        ))
        assertEquals(1, records[1].time.second)
        assertEquals(12, computeRestMinutes(records))
    }

    @Test
    fun `given rest off stored a second later in a shared minute when computeRestMinutes then full minutes are counted`() {
        val records = withStoredSeconds(listOf(
            record("11:20", REST_ON),
            record("11:32", MEET),
            record("11:32", REST_OFF),
        ))
        assertEquals(12, computeRestMinutes(records))
    }

    @Test
    fun `given several closed rests when computing rest then minutes and divisions are summed`() {
        val records = listOf(
            record("10:00", REST_ON), record("10:10", REST_OFF),
            record("11:00", REST_ON), record("11:05", REST_OFF),
        )
        assertEquals(15, computeRestMinutes(records))
        assertEquals(2, computeRestDivisions(records))
    }

    @Test
    fun `given rest across midnight when computeRestMinutes then minutes are counted across days`() {
        val records = listOf(
            record("23:50", REST_ON),
            record("00:10", REST_OFF, nextDay),
        )
        assertEquals(20, computeRestMinutes(records))
    }

    @Test
    fun `given open rest when computing rest then counted up to last record and no division used`() {
        val records = listOf(record("11:20", REST_ON), record("11:25", MEET))
        assertEquals(5, computeRestMinutes(records))
        assertEquals(0, computeRestDivisions(records))
    }

    @Test
    fun `given rest off without rest on when computing rest then it is ignored`() {
        val records = listOf(record("11:20", REST_OFF), record("11:30", LIFT))
        assertEquals(0, computeRestMinutes(records))
        assertEquals(0, computeRestDivisions(records))
    }

    @Test
    fun `given no records when computing rest then zero`() {
        assertEquals(0, computeRestMinutes(emptyList()))
        assertEquals(0, computeLiveRestMinutes(emptyList(), timeAt("12:00")))
    }

    // --- computeLiveRestMinutes ---

    @Test
    fun `given on rest when computeLiveRestMinutes then counted up to now`() {
        val records = listOf(record("11:20", GET_OFF), record("11:20", REST_ON))
        assertEquals(10, computeLiveRestMinutes(records, timeAt("11:30")))
    }

    @Test
    fun `given record added during rest when computeLiveRestMinutes then it is not double counted`() {
        val records = listOf(record("11:20", REST_ON), record("11:25", MEET))
        assertEquals(10, computeLiveRestMinutes(records, timeAt("11:30")))
    }

    @Test
    fun `given rest on stored a second later when timer ticks at next minute then one minute is counted`() {
        val records = withStoredSeconds(listOf(record("11:20", GET_OFF), record("11:20", REST_ON)))
        assertEquals(1, computeLiveRestMinutes(records, timeAt("11:21:00")))
    }

    @Test
    fun `given closed rests and open rest when computeLiveRestMinutes then closed rests are included`() {
        val records = listOf(
            record("10:00", REST_ON), record("10:10", REST_OFF),
            record("11:20", REST_ON),
        )
        assertEquals(15, computeLiveRestMinutes(records, timeAt("11:25")))
    }

    @Test
    fun `given not on rest when computeLiveRestMinutes then now is ignored`() {
        val records = listOf(record("11:20", REST_ON), record("11:30", REST_OFF))
        assertEquals(10, computeLiveRestMinutes(records, timeAt("12:00")))
    }

    @Test
    fun `given finished with open rest when computeLiveRestMinutes then counted up to last record`() {
        val records = listOf(record("11:20", REST_ON), record("11:40", FINISH))
        assertEquals(20, computeLiveRestMinutes(records, timeAt("12:00")))
    }

    @Test
    fun `given retired with open rest when computeLiveRestMinutes then counted up to last record`() {
        val records = listOf(record("11:20", REST_ON), record("11:40", RETIRE))
        assertEquals(20, computeLiveRestMinutes(records, timeAt("12:00")))
    }

    @Test
    fun `given record with future time during rest when computeLiveRestMinutes then not less than static total`() {
        val records = listOf(record("11:20", REST_ON), record("11:40", MEET))
        assertEquals(20, computeLiveRestMinutes(records, timeAt("11:30")))
    }

    @Test
    fun `given on rest when timer ticks every minute then counter increases by one each minute`() {
        val records = withStoredSeconds(listOf(record("11:20", GET_OFF), record("11:20", REST_ON)))
        val values = (21..30).map { minute -> computeLiveRestMinutes(records, timeAt("11:$minute:00")) }
        assertEquals((1..10).toList(), values)
    }

    // --- Real race replay: ГМА 186, 11 October 2026 ---

    private val gma186 = listOf(
        record("08:00", START), record("08:00", WALK), record("08:02", WALK_END), record("08:10", FREE_TEXT),
        record("08:17", LIFT), record("08:26", MEET), record("08:31", MEET), record("08:36", GET_OFF),
        record("08:36", WALK), record("08:40", WALK_END), record("08:47", MEET), record("08:49", LIFT),
        record("09:10", GET_OFF), record("09:12", LIFT), record("09:20", MEET), record("09:24", CHECKPOINT),
        record("09:40", GET_OFF), record("09:46", LIFT), record("09:50", GET_OFF), record("10:15", WALK),
        record("10:20", WALK_END), record("10:31", LIFT), record("10:57", MEET),
        record("10:58", REST_ON), record("11:04", REST_OFF),
        record("11:08", GET_OFF), record("11:11", LIFT), record("11:16", MEET), record("11:20", GET_OFF),
        record("11:20", REST_ON), record("11:32", REST_OFF),
        record("11:35", REST_ON), record("11:45", REST_OFF),
        record("11:46", LIFT), record("11:50", GET_OFF), record("11:52", CHECKPOINT), record("12:05", LIFT),
        record("12:12", GET_OFF), record("12:20", LIFT), record("12:38", GET_OFF), record("12:42", LIFT),
        record("12:49", GET_OFF), record("12:57", LIFT), record("13:12", GET_OFF), record("13:12", LIFT),
        record("13:26", CHECKPOINT), record("13:26", MEET), record("13:34", GET_OFF), record("14:05", LIFT),
        record("14:22", REST_ON), record("14:27", REST_OFF),
        record("14:29", GET_OFF), record("14:31", LIFT), record("14:49", GET_OFF), record("14:49", WALK),
        record("14:53", WALK_END), record("14:54", FREE_TEXT), record("14:56", LIFT), record("15:16", GET_OFF),
        record("15:16", CHECKPOINT), record("15:22", LIFT), record("15:25", GET_OFF), record("15:36", LIFT),
        record("16:07", MEET), record("16:24", GET_OFF),
        record("16:24", REST_ON), record("16:40", REST_OFF),
        record("16:46", LIFT), record("17:04", GET_OFF), record("17:05", CHECKPOINT), record("17:16", LIFT),
        record("17:36", GET_OFF), record("17:42", LIFT), record("17:43", MEET), record("18:23", GET_OFF),
        record("18:24", WALK), record("18:27", WALK_END), record("18:30", WALK), record("18:39", WALK_END),
        record("18:39", WALK), record("18:39", CHECKPOINT), record("18:50", WALK_END), record("19:00", LIFT),
        record("19:05", GET_OFF), record("19:06", WALK), record("19:24", WALK_END), record("19:27", LIFT),
        record("19:33", GET_OFF),
        record("19:33", REST_ON), record("19:46", REST_OFF),
        record("19:48", LIFT), record("21:10", GET_OFF), record("21:10", WALK), record("21:18", WALK_END),
        record("21:23", LIFT), record("21:35", GET_OFF), record("21:35", WALK), record("21:40", WALK_END),
        record("21:40", FINISH),
        record("12:28", FREE_TEXT, LocalDate(2026, 10, 13)),
        record("12:43", FREE_TEXT, LocalDate(2026, 10, 13)),
        record("12:43", FREE_TEXT, LocalDate(2026, 10, 13)),
    )

    @Test
    fun `given GMA 186 log with stored seconds when computing rest then total is 01 02 over 6`() {
        val records = withStoredSeconds(gma186)
        assertTrue(records.any { it.type == REST_ON && it.time.second > 0 }, "fixture should contain bumped Rest on")

        val minutes = computeRestMinutes(records)
        val divisions = computeRestDivisions(records)
        assertEquals("01:02/6", "${formatMinutes(minutes)}/$divisions")
    }

    @Test
    fun `given GMA 186 log without stored seconds when computeRestMinutes then total is 62`() {
        assertEquals(62, computeRestMinutes(gma186))
    }

    @Test
    fun `given GMA 186 log after finish when computeLiveRestMinutes then equals static total`() {
        val records = withStoredSeconds(gma186)
        assertEquals(62, computeLiveRestMinutes(records, timeAt("18:00", LocalDate(2026, 10, 14))))
    }

    @Test
    fun `given GMA 186 records in random order when computeRestMinutes then stored seconds restore order`() {
        val records = withStoredSeconds(gma186).shuffled()
        assertEquals(62, computeRestMinutes(records))
    }
}
