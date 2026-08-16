package com.checklisted.app.ui.goal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checklisted.app.domain.history.HeatmapDay
import com.checklisted.app.domain.history.HistoryCalculator
import com.checklisted.app.domain.history.HistoryGoal
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.period.PeriodCalculator
import com.checklisted.app.domain.period.TodayClock
import com.checklisted.app.domain.period.ZoneProvider
import com.checklisted.app.domain.repository.CompletionRepository
import com.checklisted.app.domain.repository.GoalRepository
import com.checklisted.app.domain.repository.SettingsRepository
import com.checklisted.app.domain.streak.GoalStats
import com.checklisted.app.domain.streak.StreakCalculator
import com.checklisted.app.ui.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

data class GoalDetailUiState(
    val goal: Goal? = null,
    val stats: GoalStats? = null,
    val heatmap: List<HeatmapDay> = emptyList(),
    val completedKeys: Set<PeriodKey> = emptySet(),
    val today: LocalDate? = null,
    val isLoading: Boolean = true,
    val isMissing: Boolean = false,
)

@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val completionRepository: CompletionRepository,
    private val settingsRepository: SettingsRepository,
    private val todayClock: TodayClock,
    private val zoneProvider: ZoneProvider,
    private val clock: Clock,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val goalId: String = requireNotNull(savedStateHandle[Destination.GoalDetail.ARG_GOAL_ID]) {
        "GoalDetail requires a goal id"
    }

    /** Rebuilt whenever the week start changes, since that re-keys weekly periods. */
    private var periods = PeriodCalculator()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<GoalDetailUiState> =
        combine(todayClock.today, settingsRepository.weekStart) { date, weekStart ->
            date to PeriodCalculator(weekStart)
        }.flatMapLatest { (date, periodCalculator) ->
            periods = periodCalculator
            val streaks = StreakCalculator(periodCalculator)
            val history = HistoryCalculator(periodCalculator)

            combine(
                goalRepository.observeGoal(goalId),
                completionRepository.observeCompletionsForGoal(goalId),
            ) { goal, completions ->
                if (goal == null) {
                    return@combine GoalDetailUiState(isLoading = false, isMissing = true)
                }

                val completedKeys = completions.mapTo(mutableSetOf()) { it.periodKey }
                val createdOn = goal.createdAt.atZone(zoneProvider.current()).toLocalDate()

                GoalDetailUiState(
                    goal = goal,
                    today = date,
                    isLoading = false,
                    completedKeys = completedKeys,
                    stats = streaks.stats(
                        recurrence = goal.recurrence,
                        completedKeys = completedKeys,
                        today = date,
                        createdOn = createdOn,
                    ),
                    heatmap = history.heatmap(
                        goals = listOf(
                            HistoryGoal(
                                id = goal.id,
                                recurrence = goal.recurrence,
                                createdOn = createdOn,
                                completedKeys = completedKeys,
                            ),
                        ),
                        from = history.windowStart(date),
                        to = date,
                    ),
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = GoalDetailUiState(),
        )

    /**
     * Toggles the period containing [date], which may be well in the past.
     *
     * Retroactive edits go through the same repository call as ticking today's box —
     * the period key is just derived from a different date.
     */
    fun toggleDay(date: LocalDate) {
        val state = uiState.value
        val goal = state.goal ?: return
        val key = periods.periodKey(goal.recurrence, date)

        viewModelScope.launch {
            completionRepository.setCompleted(
                goalId = goal.id,
                periodKey = key,
                completed = key !in state.completedKeys,
                at = clock.instant(),
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
