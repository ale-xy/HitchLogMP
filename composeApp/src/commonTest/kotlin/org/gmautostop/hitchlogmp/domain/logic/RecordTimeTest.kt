package org.gmautostop.hitchlogmp.domain.logic

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class RecordTimeTest {

    private val raceDay = LocalDate(2026, 10, 11)

    /** [time] is "HH:mm" or "HH:mm:ss" on [date]. */
    private fun timeAt(time: String, date: LocalDate = raceDay) =
        LocalDateTime(date, LocalTime.parse(time))

    @Test
    fun `given no records when storedTimeFor then entered time is kept`() {
        assertEquals(timeAt("11:20"), storedTimeFor(timeAt("11:20"), emptyList()))
    }

    @Test
    fun `given records only in other minutes when storedTimeFor then entered time is kept`() {
        val existing = listOf(timeAt("11:19:00"), timeAt("11:19:05"), timeAt("11:21:00"), timeAt("11:20", LocalDate(2026, 10, 12)))
        assertEquals(timeAt("11:20"), storedTimeFor(timeAt("11:20"), existing))
    }

    @Test
    fun `given one record in the minute when storedTimeFor then one second after it`() {
        assertEquals(timeAt("11:20:01"), storedTimeFor(timeAt("11:20"), listOf(timeAt("11:20:00"))))
    }

    @Test
    fun `given several records in the minute when storedTimeFor then one second after the latest`() {
        val existing = listOf(timeAt("11:20:02"), timeAt("11:20:00"), timeAt("11:20:01"))
        assertEquals(timeAt("11:20:03"), storedTimeFor(timeAt("11:20"), existing))
    }

    @Test
    fun `given records added one by one in the same minute when storedTimeFor then they keep insertion order`() {
        val stored = mutableListOf<LocalDateTime>()
        repeat(3) { stored += storedTimeFor(timeAt("18:39"), stored) }
        assertEquals(listOf(timeAt("18:39:00"), timeAt("18:39:01"), timeAt("18:39:02")), stored)
    }

    @Test
    fun `given edited record not excluded from existing when storedTimeFor then it bumps itself`() {
        // Why callers must exclude the edited record: Rest on stored at 11:20:01 after Высадка at 11:20:00
        val withItself = listOf(timeAt("11:20:00"), timeAt("11:20:01"))
        val withoutItself = listOf(timeAt("11:20:00"))
        assertEquals(timeAt("11:20:02"), storedTimeFor(timeAt("11:20"), withItself))
        assertEquals(timeAt("11:20:01"), storedTimeFor(timeAt("11:20"), withoutItself))
    }

    @Test
    fun `given minute already filled to 59 seconds when storedTimeFor then spills into next minute`() {
        // Documents current behaviour; unrealistic during a race (60 records in one minute)
        assertEquals(timeAt("11:21:00"), storedTimeFor(timeAt("11:20"), listOf(timeAt("11:20:59"))))
    }

    @Test
    fun `given last minute of the day filled when storedTimeFor then spills into next day`() {
        assertEquals(
            timeAt("00:00:00", LocalDate(2026, 10, 12)),
            storedTimeFor(timeAt("23:59"), listOf(timeAt("23:59:59")))
        )
    }
}
