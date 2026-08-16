package com.checklisted.app.ui

import com.checklisted.app.domain.model.Completion
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.model.ThemeMode
import com.checklisted.app.domain.model.WeekStart
import com.checklisted.app.domain.period.TodayClock
import com.checklisted.app.domain.period.ZoneProvider
import com.checklisted.app.domain.reminder.ReminderPlanner
import com.checklisted.app.domain.repository.CompletionRepository
import com.checklisted.app.domain.repository.GoalRepository
import com.checklisted.app.domain.repository.Settings
import com.checklisted.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

/**
 * In-memory stand-ins for the whole data layer.
 *
 * Hand-written rather than mocked so the tests assert against real behaviour —
 * `setCompleted` genuinely inserts and removes, which is what makes the toggle tests
 * meaningful.
 */
class FakeGoalRepository : GoalRepository {
    val goals = MutableStateFlow<List<Goal>>(emptyList())
    var savedOrder: List<String>? = null
        private set

    override fun observeGoals(includeArchived: Boolean): Flow<List<Goal>> =
        goals.map { list -> if (includeArchived) list else list.filterNot { it.isArchived } }

    override fun observeGoal(id: String): Flow<Goal?> = goals.map { list -> list.find { it.id == id } }

    override suspend fun createGoal(
        title: String,
        description: String?,
        recurrence: Recurrence,
        colorTag: String,
    ): String {
        val id = UUID.randomUUID().toString()
        goals.value = goals.value + Goal(
            id = id,
            title = title,
            description = description,
            recurrence = recurrence,
            colorTag = colorTag,
            position = goals.value.size,
            createdAt = Instant.EPOCH,
        )
        return id
    }

    override suspend fun updateGoal(goal: Goal) {
        goals.value = goals.value.map { if (it.id == goal.id) goal else it }
    }

    override suspend fun setArchived(id: String, archived: Boolean) {
        goals.value = goals.value.map { if (it.id == id) it.copy(isArchived = archived) else it }
    }

    override suspend fun deleteGoal(id: String) {
        goals.value = goals.value.filterNot { it.id == id }
    }

    override suspend fun reorderGoals(orderedIds: List<String>) {
        savedOrder = orderedIds
    }
}

class FakeCompletionRepository : CompletionRepository {
    val completions = MutableStateFlow<List<Completion>>(emptyList())

    override fun observeCompletions(periodKeys: Collection<PeriodKey>): Flow<List<Completion>> =
        completions.map { list -> list.filter { it.periodKey in periodKeys } }

    override fun observeCompletionsForGoal(goalId: String): Flow<List<Completion>> =
        completions.map { list -> list.filter { it.goalId == goalId } }

    override fun observeAllCompletions(): Flow<List<Completion>> = completions

    override suspend fun setCompleted(
        goalId: String,
        periodKey: PeriodKey,
        completed: Boolean,
        at: Instant,
    ) {
        val existing = completions.value.any { it.goalId == goalId && it.periodKey == periodKey }
        completions.value = when {
            // Mirrors the unique index: a repeated insert is a no-op, never a duplicate.
            completed && existing -> completions.value
            completed -> completions.value + Completion(UUID.randomUUID().toString(), goalId, periodKey, at)
            else -> completions.value.filterNot { it.goalId == goalId && it.periodKey == periodKey }
        }
    }
}

class FakeSettingsRepository : SettingsRepository {
    val state = MutableStateFlow(Settings())

    override val settings: Flow<Settings> = state
    override val weekStart: Flow<WeekStart> = state.map { it.weekStart }

    override suspend fun setWeekStart(weekStart: WeekStart) {
        state.value = state.value.copy(weekStart = weekStart)
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        state.value = state.value.copy(themeMode = themeMode)
    }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        state.value = state.value.copy(reminderEnabled = enabled)
    }

    override suspend fun setReminderTime(time: LocalTime) {
        state.value = state.value.copy(reminderTime = time)
    }

    fun set(value: WeekStart) {
        state.value = state.value.copy(weekStart = value)
    }
}

/** Records what the screen asked for, without touching WorkManager. */
class FakeReminderPlanner : ReminderPlanner {
    val calls = mutableListOf<Pair<Boolean, LocalTime>>()

    val last: Pair<Boolean, LocalTime>? get() = calls.lastOrNull()

    override fun apply(enabled: Boolean, time: LocalTime) {
        calls += enabled to time
    }
}

class FakeTodayClock(initial: LocalDate) : TodayClock {
    private val state = MutableStateFlow(initial)
    override val today: Flow<LocalDate> = state

    fun set(date: LocalDate) {
        state.value = date
    }
}

class FakeZoneProvider(private val zone: ZoneId = ZoneId.of("UTC")) : ZoneProvider {
    override fun current(): ZoneId = zone
}
