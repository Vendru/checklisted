package com.checklisted.app.domain.history

import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.period.PeriodCalculator
import java.time.LocalDate

/** A goal reduced to what the heatmap needs to know about it. */
data class HistoryGoal(
    val id: String,
    val recurrence: Recurrence,
    val createdOn: LocalDate,
    val completedKeys: Set<PeriodKey>,
)

/**
 * One day of the heatmap.
 *
 * [total] counts only the goals that already existed on this date, so the months
 * before a goal was created read as empty rather than as failures.
 */
data class HeatmapDay(
    val date: LocalDate,
    val completed: Int,
    val total: Int,
) {
    val fraction: Float get() = if (total == 0) 0f else completed.toFloat() / total

    val isTracked: Boolean get() = total > 0
}

/**
 * Builds the heatmap grids.
 *
 * A weekly or monthly goal contributes to **every** day of the period it was
 * completed in — the point of the grid is to show when the user was on track, and a
 * monthly goal ticked on the 3rd was on track for the whole month, not for one day.
 */
class HistoryCalculator(
    private val periods: PeriodCalculator,
) {
    fun heatmap(goals: List<HistoryGoal>, from: LocalDate, to: LocalDate): List<HeatmapDay> =
        periods.daysBetween(from, to).map { date ->
            var completed = 0
            var total = 0
            goals.forEach { goal ->
                val done = periods.periodKey(goal.recurrence, date) in goal.completedKeys
                // Predating the goal only hides a day that has nothing on it. Marking
                // one retroactively is allowed, and a marked day that stayed blank
                // because the goal was younger than it would just look like the tap
                // had failed.
                if (goal.createdOn.isAfter(date) && !done) return@forEach
                total++
                if (done) completed++
            }
            HeatmapDay(date = date, completed = completed, total = total)
        }

    /**
     * First day of the window ending at [to], aligned to the start of a week so the
     * grid's rows are whole weeks and the columns line up by weekday.
     */
    fun windowStart(to: LocalDate, months: Long = DEFAULT_MONTHS): LocalDate =
        periods.periodStart(Recurrence.WEEKLY, to.minusMonths(months))

    private companion object {
        const val DEFAULT_MONTHS = 3L
    }
}
