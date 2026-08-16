package com.checklisted.app.domain.period

import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.model.WeekStart
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

/**
 * Turns dates into [PeriodKey]s and walks backwards through periods.
 *
 * Deliberately free of Android and of any ambient clock: every entry point takes
 * the date it should work from, so the whole thing is exercised by plain JVM tests
 * with fixed dates rather than by whatever day the test happens to run on.
 */
class PeriodCalculator(
    val weekStart: WeekStart = WeekStart.Default,
) {
    /**
     * ISO-style week rules generalised to an arbitrary first day.
     *
     * With [WeekStart.MONDAY] this is exactly ISO-8601. The `minimalDaysInFirstWeek`
     * of 4 is what makes a week belong to the year that owns most of it, which is
     * why 2024-12-30 is week 1 of 2025 rather than week 53 of 2024.
     */
    private val weekFields = WeekFields.of(weekStart.dayOfWeek, MINIMAL_DAYS_IN_FIRST_WEEK)

    /** The user's current local date. The zone is passed in, never assumed to be UTC. */
    fun today(now: Instant, zone: ZoneId): LocalDate = now.atZone(zone).toLocalDate()

    fun periodKey(recurrence: Recurrence, date: LocalDate): PeriodKey = when (recurrence) {
        Recurrence.DAILY -> PeriodKey(DAILY_FORMAT.format(date.year, date.monthValue, date.dayOfMonth))
        Recurrence.WEEKLY -> PeriodKey(
            WEEKLY_FORMAT.format(
                date.get(weekFields.weekBasedYear()),
                date.get(weekFields.weekOfWeekBasedYear()),
            ),
        )
        Recurrence.MONTHLY -> PeriodKey(MONTHLY_FORMAT.format(date.year, date.monthValue))
    }

    /** First day of the period [date] falls in. */
    fun periodStart(recurrence: Recurrence, date: LocalDate): LocalDate = when (recurrence) {
        Recurrence.DAILY -> date
        Recurrence.WEEKLY -> date.with(TemporalAdjusters.previousOrSame(weekStart.dayOfWeek))
        Recurrence.MONTHLY -> date.withDayOfMonth(1)
    }

    /** First day of the *next* period — the moment the list on screen has to change. */
    fun nextPeriodStart(recurrence: Recurrence, date: LocalDate): LocalDate {
        val start = periodStart(recurrence, date)
        return when (recurrence) {
            Recurrence.DAILY -> start.plusDays(1)
            Recurrence.WEEKLY -> start.plusWeeks(1)
            Recurrence.MONTHLY -> start.plusMonths(1)
        }
    }

    /**
     * Start of the period [periodsAgo] periods before the one containing [date].
     *
     * Normalises to the period start before stepping back, so monthly arithmetic
     * cannot drift: stepping back from the 31st would otherwise land on the 28th and
     * stay there.
     */
    fun periodStartBefore(recurrence: Recurrence, date: LocalDate, periodsAgo: Long): LocalDate {
        val start = periodStart(recurrence, date)
        return when (recurrence) {
            Recurrence.DAILY -> start.minusDays(periodsAgo)
            Recurrence.WEEKLY -> start.minusWeeks(periodsAgo)
            Recurrence.MONTHLY -> start.minusMonths(periodsAgo)
        }
    }

    /**
     * Keys for the [count] most recent periods, newest first, starting with the one
     * containing [date].
     */
    fun recentPeriodKeys(recurrence: Recurrence, date: LocalDate, count: Int): List<PeriodKey> =
        (0 until count).map { periodKey(recurrence, periodStartBefore(recurrence, date, it.toLong())) }

    /** Every day from [from] to [to] inclusive, oldest first. Used by the heatmap. */
    fun daysBetween(from: LocalDate, to: LocalDate): List<LocalDate> {
        if (from.isAfter(to)) return emptyList()
        return generateSequence(from) { it.plusDays(1) }
            .takeWhile { !it.isAfter(to) }
            .toList()
    }

    /**
     * The instant the current period ends, in [zone].
     *
     * Computed as local midnight of the next period's first day, so it follows the
     * user's calendar across DST shifts instead of adding a fixed 24 hours.
     */
    fun nextRolloverAt(recurrence: Recurrence, date: LocalDate, zone: ZoneId): Instant =
        nextPeriodStart(recurrence, date).atStartOfDay(zone).toInstant()

    /**
     * The soonest rollover across all recurrences, which for any date is simply the
     * next local midnight — a day boundary is also a week and month boundary.
     */
    fun nextDayRolloverAt(date: LocalDate, zone: ZoneId): Instant =
        nextRolloverAt(Recurrence.DAILY, date, zone)

    private companion object {
        const val MINIMAL_DAYS_IN_FIRST_WEEK = 4
        const val DAILY_FORMAT = "%04d-%02d-%02d"
        const val WEEKLY_FORMAT = "%04d-W%02d"
        const val MONTHLY_FORMAT = "%04d-%02d"
    }
}
