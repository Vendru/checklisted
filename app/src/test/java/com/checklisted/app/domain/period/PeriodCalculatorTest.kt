package com.checklisted.app.domain.period

import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.model.WeekStart
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class PeriodCalculatorTest {
    private val mondayWeeks = PeriodCalculator(WeekStart.MONDAY)
    private val sundayWeeks = PeriodCalculator(WeekStart.SUNDAY)

    private fun date(iso: String) = LocalDate.parse(iso)

    private fun PeriodCalculator.key(recurrence: Recurrence, iso: String) =
        periodKey(recurrence, date(iso)).value

    // region key formats

    @Test
    fun `daily key is the iso date`() {
        assertEquals("2026-08-16", mondayWeeks.key(Recurrence.DAILY, "2026-08-16"))
    }

    @Test
    fun `daily key zero-pads single digit months and days`() {
        assertEquals("2026-01-05", mondayWeeks.key(Recurrence.DAILY, "2026-01-05"))
    }

    @Test
    fun `monthly key is year and month`() {
        assertEquals("2026-08", mondayWeeks.key(Recurrence.MONTHLY, "2026-08-16"))
        assertEquals("2026-01", mondayWeeks.key(Recurrence.MONTHLY, "2026-01-31"))
    }

    @Test
    fun `weekly key is the iso week`() {
        // 2026-08-16 is a Sunday, the last day of ISO week 33.
        assertEquals("2026-W33", mondayWeeks.key(Recurrence.WEEKLY, "2026-08-16"))
    }

    @Test
    fun `weekly key zero-pads the week number`() {
        assertEquals("2026-W01", mondayWeeks.key(Recurrence.WEEKLY, "2026-01-01"))
    }

    // endregion

    // region week boundaries

    @Test
    fun `monday starts a new week`() {
        assertEquals("2026-W33", mondayWeeks.key(Recurrence.WEEKLY, "2026-08-16"))
        assertEquals("2026-W34", mondayWeeks.key(Recurrence.WEEKLY, "2026-08-17"))
    }

    @Test
    fun `every day of one monday week shares a key`() {
        val keys = mondayWeeks
            .daysBetween(date("2026-08-10"), date("2026-08-16"))
            .map { mondayWeeks.periodKey(Recurrence.WEEKLY, it).value }
            .distinct()

        assertEquals(listOf("2026-W33"), keys)
    }

    @Test
    fun `sunday week start shifts the boundary by one day`() {
        // The same Sunday that closes ISO week 33 opens the Sunday-based week.
        assertEquals(date("2026-08-10"), mondayWeeks.periodStart(Recurrence.WEEKLY, date("2026-08-16")))
        assertEquals(date("2026-08-16"), sundayWeeks.periodStart(Recurrence.WEEKLY, date("2026-08-16")))
    }

    @Test
    fun `sunday week start groups saturday with the preceding sunday`() {
        val start = sundayWeeks.periodStart(Recurrence.WEEKLY, date("2026-08-22"))
        assertEquals(date("2026-08-16"), start)
        assertEquals(
            sundayWeeks.key(Recurrence.WEEKLY, "2026-08-16"),
            sundayWeeks.key(Recurrence.WEEKLY, "2026-08-22"),
        )
    }

    @Test
    fun `the two week starts can agree on the number and still disagree on the boundary`() {
        // Sunday 2026-08-16 lands in week 33 under both settings, so the key alone
        // does not reveal the difference — but the periods are not the same seven
        // days, and the Monday after them splits.
        assertEquals("2026-W33", mondayWeeks.key(Recurrence.WEEKLY, "2026-08-16"))
        assertEquals("2026-W33", sundayWeeks.key(Recurrence.WEEKLY, "2026-08-16"))

        assertEquals("2026-W34", mondayWeeks.key(Recurrence.WEEKLY, "2026-08-17"))
        assertEquals("2026-W33", sundayWeeks.key(Recurrence.WEEKLY, "2026-08-17"))
    }

    // endregion

    // region year boundaries

    @Test
    fun `late december can belong to the next week-based year`() {
        // 2024-12-30 is a Monday and most of its week falls in 2025.
        assertEquals("2025-W01", mondayWeeks.key(Recurrence.WEEKLY, "2024-12-30"))
        assertEquals("2024-W52", mondayWeeks.key(Recurrence.WEEKLY, "2024-12-29"))
    }

    @Test
    fun `early january can belong to the previous week-based year`() {
        assertEquals("2020-W53", mondayWeeks.key(Recurrence.WEEKLY, "2021-01-01"))
        assertEquals("2021-W52", mondayWeeks.key(Recurrence.WEEKLY, "2022-01-02"))
    }

    @Test
    fun `a 53 week year is numbered through to week 53`() {
        assertEquals("2026-W53", mondayWeeks.key(Recurrence.WEEKLY, "2026-12-31"))
        assertEquals("2026-W53", mondayWeeks.key(Recurrence.WEEKLY, "2027-01-03"))
        assertEquals("2027-W01", mondayWeeks.key(Recurrence.WEEKLY, "2027-01-04"))
    }

    @Test
    fun `monthly key crosses the year boundary`() {
        assertEquals("2025-12", mondayWeeks.key(Recurrence.MONTHLY, "2025-12-31"))
        assertEquals("2026-01", mondayWeeks.key(Recurrence.MONTHLY, "2026-01-01"))
    }

    // endregion

    // region leap years

    @Test
    fun `leap day belongs to february`() {
        assertEquals("2024-02-29", mondayWeeks.key(Recurrence.DAILY, "2024-02-29"))
        assertEquals("2024-02", mondayWeeks.key(Recurrence.MONTHLY, "2024-02-29"))
    }

    @Test
    fun `stepping back a month from march lands on the first of february`() {
        val start = mondayWeeks.periodStartBefore(Recurrence.MONTHLY, date("2024-03-31"), periodsAgo = 1)
        assertEquals(date("2024-02-01"), start)
    }

    @Test
    fun `monthly steps do not drift when starting from a long month`() {
        // Naively subtracting months from the 31st would clamp to the 28th and stay
        // there, silently skipping a month further back.
        val keys = (0..4L).map {
            mondayWeeks.periodKey(
                Recurrence.MONTHLY,
                mondayWeeks.periodStartBefore(Recurrence.MONTHLY, date("2024-03-31"), it),
            ).value
        }
        assertEquals(listOf("2024-03", "2024-02", "2024-01", "2023-12", "2023-11"), keys)
    }

    // endregion

    // region period navigation

    @Test
    fun `next daily period is the following day`() {
        assertEquals(date("2026-08-17"), mondayWeeks.nextPeriodStart(Recurrence.DAILY, date("2026-08-16")))
    }

    @Test
    fun `next weekly period is the following monday`() {
        assertEquals(date("2026-08-17"), mondayWeeks.nextPeriodStart(Recurrence.WEEKLY, date("2026-08-16")))
    }

    @Test
    fun `next monthly period is the first of the following month`() {
        assertEquals(date("2026-09-01"), mondayWeeks.nextPeriodStart(Recurrence.MONTHLY, date("2026-08-16")))
    }

    @Test
    fun `recent keys are newest first and contiguous`() {
        val keys = mondayWeeks.recentPeriodKeys(Recurrence.DAILY, date("2026-03-02"), count = 4)
        assertEquals(
            listOf(
                PeriodKey("2026-03-02"),
                PeriodKey("2026-03-01"),
                PeriodKey("2026-02-28"),
                PeriodKey("2026-02-27"),
            ),
            keys,
        )
    }

    @Test
    fun `recent weekly keys step one week at a time`() {
        val keys = mondayWeeks.recentPeriodKeys(Recurrence.WEEKLY, date("2025-01-05"), count = 3)
        assertEquals(listOf(PeriodKey("2025-W01"), PeriodKey("2024-W52"), PeriodKey("2024-W51")), keys)
    }

    @Test
    fun `days between is inclusive on both ends`() {
        val days = mondayWeeks.daysBetween(date("2026-08-14"), date("2026-08-16"))
        assertEquals(listOf(date("2026-08-14"), date("2026-08-15"), date("2026-08-16")), days)
    }

    @Test
    fun `days between is empty when the range is inverted`() {
        assertTrue(mondayWeeks.daysBetween(date("2026-08-16"), date("2026-08-14")).isEmpty())
    }

    // endregion

    // region timezone handling

    @Test
    fun `today is derived from the local zone not from utc`() {
        // 2026-08-16T23:30Z is already the 17th in Tokyo and still the 16th in New
        // York. London is not a useful counterexample here: in August it runs on BST,
        // so it has already turned over too.
        val instant = Instant.parse("2026-08-16T23:30:00Z")

        assertEquals(date("2026-08-16"), mondayWeeks.today(instant, ZoneId.of("America/New_York")))
        assertEquals(date("2026-08-17"), mondayWeeks.today(instant, ZoneId.of("Asia/Tokyo")))
    }

    @Test
    fun `today in sao paulo is the previous day late in the evening utc`() {
        val instant = Instant.parse("2026-08-17T01:00:00Z")
        assertEquals(date("2026-08-16"), mondayWeeks.today(instant, ZoneId.of("America/Sao_Paulo")))
    }

    @Test
    fun `rollover is local midnight not a fixed 24 hours`() {
        val zone = ZoneId.of("America/Sao_Paulo")
        val rollover = mondayWeeks.nextDayRolloverAt(date("2026-08-16"), zone)

        assertEquals(date("2026-08-17").atStartOfDay(zone).toInstant(), rollover)
    }

    @Test
    fun `rollover across a spring forward transition is still local midnight`() {
        // Lord Howe shifts by 30 minutes; a fixed 24-hour step would miss the boundary.
        val zone = ZoneId.of("Australia/Lord_Howe")
        val date = date("2026-10-04")
        val rollover = mondayWeeks.nextDayRolloverAt(date, zone)

        assertEquals(date.plusDays(1).atStartOfDay(zone).toInstant(), rollover)
    }

    @Test
    fun `weekly rollover lands on the start of the next week`() {
        val zone = ZoneId.of("America/Sao_Paulo")
        val rollover = mondayWeeks.nextRolloverAt(Recurrence.WEEKLY, date("2026-08-16"), zone)

        assertEquals(date("2026-08-17").atStartOfDay(zone).toInstant(), rollover)
    }

    // endregion
}
