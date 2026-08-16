package com.checklisted.app.domain.history

import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.model.WeekStart
import com.checklisted.app.domain.period.PeriodCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class HistoryCalculatorTest {
    private val periods = PeriodCalculator(WeekStart.MONDAY)
    private val calculator = HistoryCalculator(periods)

    private fun date(iso: String) = LocalDate.parse(iso)

    private fun keys(vararg values: String) = values.map(::PeriodKey).toSet()

    private fun goal(
        id: String = "g",
        recurrence: Recurrence = Recurrence.DAILY,
        createdOn: String = "2020-01-01",
        completed: Set<PeriodKey> = emptySet(),
    ) = HistoryGoal(id, recurrence, date(createdOn), completed)

    @Test
    fun `a daily completion lights up exactly one day`() {
        val days = calculator.heatmap(
            goals = listOf(goal(completed = keys("2026-08-14"))),
            from = date("2026-08-13"),
            to = date("2026-08-15"),
        )

        assertEquals(listOf(0, 1, 0), days.map { it.completed })
        assertEquals(listOf(1, 1, 1), days.map { it.total })
    }

    @Test
    fun `a weekly completion lights up its whole week`() {
        // 2026-W33 runs Monday 10 August through Sunday 16 August.
        val days = calculator.heatmap(
            goals = listOf(goal(recurrence = Recurrence.WEEKLY, completed = keys("2026-W33"))),
            from = date("2026-08-09"),
            to = date("2026-08-17"),
        )

        val lit = days.filter { it.completed == 1 }.map { it.date }
        assertEquals(date("2026-08-10"), lit.first())
        assertEquals(date("2026-08-16"), lit.last())
        assertEquals(7, lit.size)
    }

    @Test
    fun `a monthly completion lights up its whole month`() {
        val days = calculator.heatmap(
            goals = listOf(goal(recurrence = Recurrence.MONTHLY, completed = keys("2026-08"))),
            from = date("2026-07-30"),
            to = date("2026-09-02"),
        )

        assertTrue(days.filter { it.date.month.value == 8 }.all { it.completed == 1 })
        assertTrue(days.filter { it.date.month.value != 8 }.all { it.completed == 0 })
    }

    @Test
    fun `days before a goal existed are not counted against it`() {
        val days = calculator.heatmap(
            goals = listOf(goal(createdOn = "2026-08-14")),
            from = date("2026-08-12"),
            to = date("2026-08-15"),
        )

        assertEquals(listOf(0, 0, 1, 1), days.map { it.total })
        assertFalse(days.first().isTracked)
        assertTrue(days.last().isTracked)
        // An untracked day reads as blank, not as 0% completed.
        assertEquals(0f, days.first().fraction, 0f)
    }

    @Test
    fun `the day a goal is created already counts`() {
        val days = calculator.heatmap(
            goals = listOf(goal(createdOn = "2026-08-14")),
            from = date("2026-08-14"),
            to = date("2026-08-14"),
        )

        assertEquals(1, days.single().total)
    }

    @Test
    fun `mixed recurrences share one day's fraction`() {
        val days = calculator.heatmap(
            goals = listOf(
                goal(id = "d", completed = keys("2026-08-14")),
                goal(id = "w", recurrence = Recurrence.WEEKLY, completed = keys("2026-W33")),
                goal(id = "m", recurrence = Recurrence.MONTHLY, completed = emptySet()),
            ),
            from = date("2026-08-14"),
            to = date("2026-08-14"),
        )

        val day = days.single()
        assertEquals(2, day.completed)
        assertEquals(3, day.total)
        assertEquals(2f / 3f, day.fraction, 0.0001f)
    }

    @Test
    fun `an empty goal list produces tracked-free days rather than zeroes`() {
        val days = calculator.heatmap(emptyList(), date("2026-08-14"), date("2026-08-15"))

        assertEquals(2, days.size)
        assertTrue(days.none { it.isTracked })
    }

    @Test
    fun `the window covers three months and starts on a week boundary`() {
        val start = calculator.windowStart(date("2026-08-16"))

        assertEquals(DayOfWeek.MONDAY, start.dayOfWeek)
        assertTrue(start <= date("2026-05-16"))
        assertTrue(start > date("2026-05-09"))
    }

    @Test
    fun `the window is a whole number of weeks`() {
        val to = date("2026-08-16")
        val days = calculator.heatmap(listOf(goal()), calculator.windowStart(to), to)

        // 2026-08-16 is a Sunday, so a Monday-aligned start makes the range divide
        // evenly into rows of seven.
        assertEquals(0, days.size % 7)
    }

    @Test
    fun `an inverted range produces nothing`() {
        assertTrue(calculator.heatmap(listOf(goal()), date("2026-08-16"), date("2026-08-14")).isEmpty())
    }
}
