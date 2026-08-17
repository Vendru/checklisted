package com.checklisted.app.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.GoalStatus
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.period.PeriodCalculator
import com.checklisted.app.domain.period.TodayClock
import com.checklisted.app.domain.repository.CompletionRepository
import com.checklisted.app.domain.repository.GoalRepository
import com.checklisted.app.domain.repository.SettingsRepository
import com.checklisted.app.domain.streak.StreakCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

/** One recurrence's worth of the Today screen. */
data class TodaySection(
    val recurrence: Recurrence,
    val periodKey: PeriodKey,
    val goals: List<GoalStatus>,
) {
    val completed: Int = goals.count { it.isCompleted }
    val total: Int = goals.size
    val progress: Float = if (total == 0) 0f else completed.toFloat() / total
}

data class TodayUiState(
    val date: LocalDate? = null,
    val sections: List<TodaySection> = emptyList(),
    /** Current run length per goal id. Absent means zero. */
    val streaks: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true,
) {
    /** No goals at all, as opposed to goals that merely have nothing done yet. */
    val hasNoGoals: Boolean = !isLoading && sections.all { it.total == 0 }
}

@HiltViewModel
class TodayViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val completionRepository: CompletionRepository,
    private val settingsRepository: SettingsRepository,
    private val todayClock: TodayClock,
    private val clock: Clock,
) : ViewModel() {
    /**
     * Order held locally while a drag is in flight.
     *
     * A drag has to redraw at finger speed, so the move is applied here first and
     * written to the database when the finger lifts.
     */
    private val pendingOrder = MutableStateFlow<List<String>?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TodayUiState> =
        combine(todayClock.today, settingsRepository.weekStart) { date, weekStart ->
            date to PeriodCalculator(weekStart)
        }.flatMapLatest { (date, periods) ->
            // One key per recurrence: on any given day a daily, weekly and monthly
            // goal are each asking about a different period.
            val keysByRecurrence = Recurrence.entries.associateWith { periods.periodKey(it, date) }

            // The whole history, not just today's periods: a streak is by definition a
            // question about the past. Same flow the history screen already reads, and
            // the run is computed by the same calculator the detail screen uses — this
            // screen was the only one hiding a number it could have shown.
            combine(
                goalRepository.observeGoals(),
                completionRepository.observeCompletions(keysByRecurrence.values),
                completionRepository.observeAllCompletions(),
                pendingOrder,
            ) { goals, completions, allCompletions, order ->
                val completedGoalIds = completions.mapTo(mutableSetOf()) { it.goalId to it.periodKey }
                val ordered = applyPendingOrder(goals, order)

                val streakCalculator = StreakCalculator(periods)
                val keysByGoal = allCompletions.groupBy { it.goalId }
                    .mapValues { (_, rows) -> rows.mapTo(mutableSetOf()) { it.periodKey } }
                val streaks = ordered.associate { goal ->
                    goal.id to streakCalculator.currentStreak(
                        recurrence = goal.recurrence,
                        completedKeys = keysByGoal[goal.id].orEmpty(),
                        today = date,
                    )
                }

                TodayUiState(
                    date = date,
                    streaks = streaks,
                    isLoading = false,
                    sections = Recurrence.entries.map { recurrence ->
                        val key = keysByRecurrence.getValue(recurrence)
                        TodaySection(
                            recurrence = recurrence,
                            periodKey = key,
                            goals = ordered
                                .filter { it.recurrence == recurrence }
                                .map { goal ->
                                    GoalStatus(
                                        goal = goal,
                                        periodKey = key,
                                        isCompleted = (goal.id to key) in completedGoalIds,
                                    )
                                },
                        )
                    },
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = TodayUiState(),
        )

    fun toggle(status: GoalStatus) {
        viewModelScope.launch {
            completionRepository.setCompleted(
                goalId = status.goal.id,
                periodKey = status.periodKey,
                completed = !status.isCompleted,
                at = clock.instant(),
            )
        }
    }

    fun delete(goalId: String) {
        viewModelScope.launch { goalRepository.deleteGoal(goalId) }
    }

    /**
     * Moves [draggedId] into [targetId]'s slot, optimistically.
     *
     * Returns false when the move is not allowed, which the drag gesture reads to
     * decide whether to rebase its grip. Goals only reorder within their own
     * recurrence — the sections on screen are not one list.
     */
    fun moveGoal(draggedId: String, targetId: String): Boolean {
        val state = uiState.value
        val section = state.sections.firstOrNull { section ->
            section.goals.any { it.goal.id == draggedId } && section.goals.any { it.goal.id == targetId }
        } ?: return false

        val currentGlobal = state.sections.flatMap { it.goals.map { status -> status.goal.id } }
        val from = currentGlobal.indexOf(draggedId)
        val to = currentGlobal.indexOf(targetId)
        if (from < 0 || to < 0 || section.goals.size < 2) return false

        pendingOrder.value = currentGlobal.toMutableList().apply { add(to, removeAt(from)) }
        return true
    }

    /** Writes the dragged order out and hands ordering back to the database. */
    fun commitOrder() {
        val order = pendingOrder.value ?: return
        viewModelScope.launch {
            goalRepository.reorderGoals(order)
            pendingOrder.value = null
        }
    }

    fun cancelReorder() {
        pendingOrder.value = null
    }

    private fun applyPendingOrder(goals: List<Goal>, order: List<String>?): List<Goal> {
        if (order == null) return goals
        val byId = goals.associateBy { it.id }
        // Goals created mid-drag are not in the pending order; they keep their own
        // place at the end rather than disappearing.
        val reordered = order.mapNotNull(byId::get)
        val missing = goals.filter { it.id !in order.toSet() }
        return reordered + missing
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
