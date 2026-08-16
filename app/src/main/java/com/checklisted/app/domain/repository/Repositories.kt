package com.checklisted.app.domain.repository

import com.checklisted.app.domain.model.Completion
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.model.ThemeMode
import com.checklisted.app.domain.model.WeekStart
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalTime

interface GoalRepository {
    /** Active goals, ordered for display. */
    fun observeGoals(includeArchived: Boolean = false): Flow<List<Goal>>

    fun observeGoal(id: String): Flow<Goal?>

    /** Returns the new goal's id. Position is assigned at the end of the list. */
    suspend fun createGoal(
        title: String,
        description: String?,
        recurrence: Recurrence,
        colorTag: String,
    ): String

    suspend fun updateGoal(goal: Goal)

    /** Archiving keeps the goal and its whole history; it only leaves the Today list. */
    suspend fun setArchived(id: String, archived: Boolean)

    /** Deleting cascades to the goal's completions. Archiving is the reversible option. */
    suspend fun deleteGoal(id: String)

    /** Persists a new manual order. [orderedIds] is the full list, front to back. */
    suspend fun reorderGoals(orderedIds: List<String>)
}

interface CompletionRepository {
    /**
     * Completions matching any of [periodKeys].
     *
     * The Today screen passes one key per recurrence, since a daily, weekly and
     * monthly goal are each asking about a different period on the same day.
     */
    fun observeCompletions(periodKeys: Collection<PeriodKey>): Flow<List<Completion>>

    /** Whole history for one goal, for the streak and heatmap views. */
    fun observeCompletionsForGoal(goalId: String): Flow<List<Completion>>

    fun observeAllCompletions(): Flow<List<Completion>>

    /**
     * Inserts or removes the completion for one period.
     *
     * [periodKey] is not required to be the current period: correcting a forgotten
     * day from the history screen goes through the same call.
     */
    suspend fun setCompleted(
        goalId: String,
        periodKey: PeriodKey,
        completed: Boolean,
        at: Instant,
    )
}

/** Everything the user can change, and nothing that changes on its own. */
data class Settings(
    val weekStart: WeekStart = WeekStart.Default,
    val themeMode: ThemeMode = ThemeMode.Default,
    val reminderEnabled: Boolean = false,
    val reminderTime: LocalTime = DEFAULT_REMINDER_TIME,
) {
    companion object {
        /** Late enough to be a nudge about the day, early enough not to wake anyone. */
        val DEFAULT_REMINDER_TIME: LocalTime = LocalTime.of(20, 0)
    }
}

interface SettingsRepository {
    val settings: Flow<Settings>

    val weekStart: Flow<WeekStart>

    suspend fun setWeekStart(weekStart: WeekStart)

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setReminderEnabled(enabled: Boolean)

    suspend fun setReminderTime(time: LocalTime)
}
