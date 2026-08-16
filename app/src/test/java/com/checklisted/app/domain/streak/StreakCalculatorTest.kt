package com.checklisted.app.domain.streak

import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.model.WeekStart
import com.checklisted.app.domain.period.PeriodCalculator
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StreakCalculatorTest {
    private val periods = PeriodCalculator(WeekStart.MONDAY)
    private val calculator = StreakCalculator(periods)

    private fun date(iso: String) = LocalDate.parse(iso)

    private fun keys(vararg values: String) = values.map(::PeriodKey).toSet()

    /** Keys for [count] consecutive periods ending at [endingOn]. */
    private fun run(recurrence: Recurrence, endingOn: String, count: Int): Set<PeriodKey> =
        periods.recentPeriodKeys(recurrence, date(endingOn), count).toSet()

    // region current streak — the open period is neutral

    @Test
    fun `no completions means no streak`() {
        assertEquals(0, calculator.currentStreak(Recurrence.DAILY, emptySet(), date("2026-08-16")))
    }

    @Test
    fun `an unticked today does not break a run of completed days`() {
        // The rule that shapes the whole feature: at 10am with yesterday and the day
        // before done, the user is on a streak of 2, not 0.
        val completed = keys("2026-08-15", "2026-08-14")

        assertEquals(2, calculator.currentStreak(Recurrence.DAILY, completed, date("2026-08-16")))
    }

    @Test
    fun `ticking today extends the streak immediately`() {
        val completed = keys("2026-08-16", "2026-08-15", "2026-08-14")

        assertEquals(3, calculator.currentStreak(Recurrence.DAILY, completed, date("2026-08-16")))
    }

    @Test
    fun `a streak of only today counts as one`() {
        assertEquals(1, calculator.currentStreak(Recurrence.DAILY, keys("2026-08-16"), date("2026-08-16")))
    }

    @Test
    fun `a missed day breaks the run once it has closed`() {
        // 2026-08-15 was never ticked and is now in the past, so the older run is dead.
        val completed = keys("2026-08-14", "2026-08-13", "2026-08-12")

        assertEquals(0, calculator.currentStreak(Recurrence.DAILY, completed, date("2026-08-16")))
    }

    @Test
    fun `only the run touching the present counts`() {
        val completed = keys("2026-08-16", "2026-08-15") + keys("2026-08-10", "2026-08-09", "2026-08-08")

        assertEquals(2, calculator.currentStreak(Recurrence.DAILY, completed, date("2026-08-16")))
    }

    @Test
    fun `a streak survives a month boundary`() {
        val completed = keys("2026-03-01", "2026-02-28", "2026-02-27")

        assertEquals(3, calculator.currentStreak(Recurrence.DAILY, completed, date("2026-03-01")))
    }

    @Test
    fun `a streak survives a leap day`() {
        val completed = keys("2024-03-01", "2024-02-29", "2024-02-28")

        assertEquals(3, calculator.currentStreak(Recurrence.DAILY, completed, date("2024-03-01")))
    }

    @Test
    fun `weekly streak counts weeks not days`() {
        val completed = keys("2025-W01", "2024-W52", "2024-W51")

        assertEquals(3, calculator.currentStreak(Recurrence.WEEKLY, completed, date("2025-01-02")))
    }

    @Test
    fun `weekly streak is neutral about the week in progress`() {
        val completed = keys("2024-W52", "2024-W51")

        assertEquals(2, calculator.currentStreak(Recurrence.WEEKLY, completed, date("2025-01-02")))
    }

    @Test
    fun `monthly streak counts months`() {
        val completed = keys("2026-08", "2026-07", "2026-06")

        assertEquals(3, calculator.currentStreak(Recurrence.MONTHLY, completed, date("2026-08-16")))
    }

    @Test
    fun `a long daily run is counted in full`() {
        val completed = run(Recurrence.DAILY, "2026-08-16", count = 365)

        assertEquals(365, calculator.currentStreak(Recurrence.DAILY, completed, date("2026-08-16")))
    }

    // endregion

    // region retroactive edits

    @Test
    fun `filling in a forgotten day rejoins two runs`() {
        val before = keys("2026-08-16", "2026-08-14", "2026-08-13")
        assertEquals(1, calculator.currentStreak(Recurrence.DAILY, before, date("2026-08-16")))

        val after = before + keys("2026-08-15")
        assertEquals(4, calculator.currentStreak(Recurrence.DAILY, after, date("2026-08-16")))
    }

    @Test
    fun `unticking a past day splits the run`() {
        val completed = keys("2026-08-16", "2026-08-15", "2026-08-14") - keys("2026-08-15")

        assertEquals(1, calculator.currentStreak(Recurrence.DAILY, completed, date("2026-08-16")))
    }

    // endregion

    // region best streak

    @Test
    fun `best streak with no history is zero`() {
        assertEquals(0, calculator.bestStreak(Recurrence.DAILY, emptySet(), date("2026-08-16")))
    }

    @Test
    fun `best streak finds the longest past run`() {
        val old = run(Recurrence.DAILY, "2026-07-20", count = 7)
        val current = keys("2026-08-16", "2026-08-15")

        assertEquals(7, calculator.bestStreak(Recurrence.DAILY, old + current, date("2026-08-16")))
    }

    @Test
    fun `best streak equals the current streak when the current run is the longest`() {
        val completed = run(Recurrence.DAILY, "2026-08-16", count = 10)

        assertEquals(10, calculator.currentStreak(Recurrence.DAILY, completed, date("2026-08-16")))
        assertEquals(10, calculator.bestStreak(Recurrence.DAILY, completed, date("2026-08-16")))
    }

    @Test
    fun `best streak is one when nothing is consecutive`() {
        val completed = keys("2026-08-16", "2026-08-12", "2026-08-04")

        assertEquals(1, calculator.bestStreak(Recurrence.DAILY, completed, date("2026-08-16")))
    }

    @Test
    fun `best streak spans a year boundary`() {
        val completed = keys("2026-01-02", "2026-01-01", "2025-12-31", "2025-12-30")

        assertEquals(4, calculator.bestStreak(Recurrence.DAILY, completed, date("2026-01-05")))
    }

    @Test
    fun `best weekly streak spans a week-based year boundary`() {
        // 2024-W52 is followed by 2025-W01, not by a 2024-W53.
        val completed = keys("2024-W51", "2024-W52", "2025-W01")

        assertEquals(3, calculator.bestStreak(Recurrence.WEEKLY, completed, date("2025-01-15")))
    }

    // endregion

    // region completion rate

    @Test
    fun `daily rate looks back thirty closed periods`() {
        val completed = run(Recurrence.DAILY, "2026-08-15", count = 15)
        val rate = calculator.completionRate(Recurrence.DAILY, completed, date("2026-08-16"))

        // Today is untouched, so it is neither counted nor held against the goal.
        assertEquals(30, rate.total)
        assertEquals(15, rate.completed)
        assertEquals(50, rate.percent)
    }

    @Test
    fun `a completed today joins the window`() {
        val completed = run(Recurrence.DAILY, "2026-08-16", count = 31)
        val rate = calculator.completionRate(Recurrence.DAILY, completed, date("2026-08-16"))

        assertEquals(31, rate.total)
        assertEquals(31, rate.completed)
        assertEquals(100, rate.percent)
    }

    @Test
    fun `rate is clamped to the goal's lifetime`() {
        // Created three days ago: 3 of 3, not 3 of 30.
        val completed = keys("2026-08-15", "2026-08-14")
        val rate = calculator.completionRate(
            Recurrence.DAILY,
            completed,
            today = date("2026-08-16"),
            createdOn = date("2026-08-13"),
        )

        assertEquals(3, rate.total)
        assertEquals(2, rate.completed)
    }

    @Test
    fun `weekly rate uses a four week window`() {
        val rate = calculator.completionRate(Recurrence.WEEKLY, keys("2026-W32"), date("2026-08-16"))

        assertEquals(4, rate.total)
        assertEquals(1, rate.completed)
    }

    @Test
    fun `monthly rate uses a three month window`() {
        val rate = calculator.completionRate(Recurrence.MONTHLY, keys("2026-07", "2026-06"), date("2026-08-16"))

        assertEquals(3, rate.total)
        assertEquals(2, rate.completed)
    }

    @Test
    fun `empty history gives a zero rate and does not divide by zero`() {
        val rate = calculator.completionRate(Recurrence.DAILY, emptySet(), date("2026-08-16"))

        assertEquals(30, rate.total)
        assertEquals(0, rate.completed)
        assertEquals(0f, rate.fraction, 0f)
    }

    @Test
    fun `a brand new goal reports an empty rather than failing rate`() {
        val rate = calculator.completionRate(
            Recurrence.DAILY,
            emptySet(),
            today = date("2026-08-16"),
            createdOn = date("2026-08-16"),
        )

        assertEquals(0, rate.total)
        assertEquals(0f, rate.fraction, 0f)
    }

    // endregion

    // region combined stats

    @Test
    fun `stats agree with the individual calculations`() {
        val completed = run(Recurrence.DAILY, "2026-08-15", count = 5)
        val stats = calculator.stats(Recurrence.DAILY, completed, date("2026-08-16"))

        assertEquals(5, stats.currentStreak)
        assertEquals(5, stats.bestStreak)
        assertEquals(5, stats.completionRate.completed)
    }

    @Test
    fun `a completion dated in the future is ignored rather than counted`() {
        val completed = keys("2026-08-20", "2026-08-15", "2026-08-14")

        assertEquals(2, calculator.currentStreak(Recurrence.DAILY, completed, date("2026-08-16")))
        assertEquals(2, calculator.bestStreak(Recurrence.DAILY, completed, date("2026-08-16")))
    }

    // endregion
}
