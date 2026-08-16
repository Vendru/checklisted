package com.checklisted.app.domain.streak

import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.period.PeriodCalculator
import java.time.LocalDate

/** How many of the recent periods were completed. */
data class CompletionRate(
    val completed: Int,
    val total: Int,
) {
    val fraction: Float get() = if (total == 0) 0f else completed.toFloat() / total

    val percent: Int get() = Math.round(fraction * 100)
}

/** Everything the detail screen shows about one goal's history. */
data class GoalStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val completionRate: CompletionRate,
)

/**
 * Streaks and completion rates over a goal's history.
 *
 * The rule that shapes all of this: **the period currently in progress is neutral.**
 * A daily goal not yet ticked at 10am has not broken anything — the streak counts
 * the run of completed periods that have already closed, and adds the current one
 * only once it is actually completed. Counting the open period as a failure would
 * show a 0 every morning; ignoring a completed one would make ticking the box feel
 * like it did nothing.
 */
class StreakCalculator(
    private val periods: PeriodCalculator,
) {
    fun stats(
        recurrence: Recurrence,
        completedKeys: Set<PeriodKey>,
        today: LocalDate,
        createdOn: LocalDate? = null,
    ): GoalStats = GoalStats(
        currentStreak = currentStreak(recurrence, completedKeys, today),
        bestStreak = bestStreak(recurrence, completedKeys, today),
        completionRate = completionRate(recurrence, completedKeys, today, createdOn = createdOn),
    )

    /**
     * Length of the unbroken run ending at the present.
     *
     * Walks backwards from the current period. The current period contributes 1 when
     * completed and is skipped over when not — it only breaks the run once it closes.
     */
    fun currentStreak(
        recurrence: Recurrence,
        completedKeys: Set<PeriodKey>,
        today: LocalDate,
    ): Int {
        if (completedKeys.isEmpty()) return 0

        var streak = 0
        var periodsAgo = 0L

        if (isCompleted(recurrence, completedKeys, today, periodsAgo = 0)) {
            streak = 1
        }
        periodsAgo = 1

        while (isCompleted(recurrence, completedKeys, today, periodsAgo)) {
            streak++
            periodsAgo++
        }
        return streak
    }

    /**
     * Longest run anywhere in the history.
     *
     * Walks the completed periods in calendar order rather than scanning every period
     * since the goal was created, so a goal with three completions two years apart
     * costs three steps and not seven hundred.
     */
    fun bestStreak(
        recurrence: Recurrence,
        completedKeys: Set<PeriodKey>,
        today: LocalDate,
    ): Int {
        if (completedKeys.isEmpty()) return 0

        val starts = completedStarts(recurrence, completedKeys, today)
        if (starts.isEmpty()) return 0

        var best = 1
        var run = 1
        for (i in 1 until starts.size) {
            val expected = periods.nextPeriodStart(recurrence, starts[i - 1])
            run = if (starts[i] == expected) run + 1 else 1
            if (run > best) best = run
        }
        return best
    }

    /**
     * Share of recent periods completed.
     *
     * The window is the last [windowSize] **closed** periods, plus the current one if
     * it has already been completed — the same neutrality rule the streak uses, so a
     * goal cannot appear to be failing at a period that is still in progress.
     *
     * [createdOn] clamps the window to the goal's lifetime: a goal created three days
     * ago reports 3/3 rather than 3/30.
     */
    fun completionRate(
        recurrence: Recurrence,
        completedKeys: Set<PeriodKey>,
        today: LocalDate,
        windowSize: Int = defaultWindow(recurrence),
        createdOn: LocalDate? = null,
    ): CompletionRate {
        val firstPeriodStart = createdOn?.let { periods.periodStart(recurrence, it) }

        val considered = buildList {
            if (isCompleted(recurrence, completedKeys, today, periodsAgo = 0)) {
                add(periods.periodStart(recurrence, today))
            }
            for (periodsAgo in 1..windowSize) {
                val start = periods.periodStartBefore(recurrence, today, periodsAgo.toLong())
                if (firstPeriodStart != null && start.isBefore(firstPeriodStart)) break
                add(start)
            }
        }

        val completed = considered.count { periods.periodKey(recurrence, it) in completedKeys }
        return CompletionRate(completed = completed, total = considered.size)
    }

    /**
     * How many periods the rate looks back over, per recurrence.
     *
     * A single "last 30 days" window would give a monthly goal a denominator of one
     * or two, which swings between 0% and 100% on a single tick. Each recurrence gets
     * a window sized in its own periods, and the UI labels it accordingly.
     */
    fun defaultWindow(recurrence: Recurrence): Int = when (recurrence) {
        Recurrence.DAILY -> DAILY_WINDOW
        Recurrence.WEEKLY -> WEEKLY_WINDOW
        Recurrence.MONTHLY -> MONTHLY_WINDOW
    }

    private fun isCompleted(
        recurrence: Recurrence,
        completedKeys: Set<PeriodKey>,
        today: LocalDate,
        periodsAgo: Long,
    ): Boolean {
        val start = periods.periodStartBefore(recurrence, today, periodsAgo)
        return periods.periodKey(recurrence, start) in completedKeys
    }

    /**
     * Period start dates for the completed keys, oldest first.
     *
     * Derived by walking back from today and keeping the hits, so a key that no
     * longer parses under the current week-start setting simply drops out instead of
     * corrupting the run.
     */
    private fun completedStarts(
        recurrence: Recurrence,
        completedKeys: Set<PeriodKey>,
        today: LocalDate,
    ): List<LocalDate> {
        val found = mutableListOf<LocalDate>()
        var periodsAgo = 0L
        var remaining = completedKeys.size

        while (remaining > 0 && periodsAgo <= MAX_LOOKBACK_PERIODS) {
            val start = periods.periodStartBefore(recurrence, today, periodsAgo)
            if (periods.periodKey(recurrence, start) in completedKeys) {
                found += start
                remaining--
            }
            periodsAgo++
        }
        return found.asReversed()
    }

    private companion object {
        const val DAILY_WINDOW = 30
        const val WEEKLY_WINDOW = 4
        const val MONTHLY_WINDOW = 3

        /**
         * Guards the backwards walk against keys that cannot be reached from today —
         * a completion dated in the future, or one written under a different
         * week-start setting. Ten years of daily periods.
         */
        const val MAX_LOOKBACK_PERIODS = 3653L
    }
}
