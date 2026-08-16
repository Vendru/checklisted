package com.checklisted.app.ui.history

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

/** One goal's state on the day the user opened. */
data class DayGoal(
    val goal: Goal,
    val periodKey: PeriodKey,
    val isCompleted: Boolean,
)

/** The day sheet opened by tapping a heatmap cell. */
data class SelectedDay(
    val date: LocalDate,
    val goals: List<DayGoal>,
)

data class HistoryUiState(
    val heatmap: List<HeatmapDay> = emptyList(),
    val today: LocalDate? = null,
    val selectedDay: SelectedDay? = null,
    val goalCount: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val completionRepository: CompletionRepository,
    goalRepository: GoalRepository,
    settingsRepository: SettingsRepository,
    todayClock: TodayClock,
    private val zoneProvider: ZoneProvider,
    private val clock: Clock,
) : ViewModel() {
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> =
        combine(todayClock.today, settingsRepository.weekStart) { date, weekStart ->
            date to PeriodCalculator(weekStart)
        }.flatMapLatest { (date, periodCalculator) ->
            val history = HistoryCalculator(periodCalculator)

            combine(
                // Archived goals are included: their history happened, and hiding it
                // would leave holes in the grid for no reason the user can see.
                goalRepository.observeGoals(includeArchived = true),
                completionRepository.observeAllCompletions(),
                selectedDate,
            ) { goals, completions, selected ->
                val keysByGoal = completions.groupBy { it.goalId }
                    .mapValues { (_, rows) -> rows.mapTo(mutableSetOf()) { it.periodKey } }

                val historyGoals = goals.map { goal ->
                    HistoryGoal(
                        id = goal.id,
                        recurrence = goal.recurrence,
                        createdOn = goal.createdAt.atZone(zoneProvider.current()).toLocalDate(),
                        completedKeys = keysByGoal[goal.id].orEmpty(),
                    )
                }

                HistoryUiState(
                    today = date,
                    isLoading = false,
                    goalCount = goals.count { !it.isArchived },
                    heatmap = history.heatmap(
                        goals = historyGoals,
                        from = history.windowStart(date),
                        to = date,
                    ),
                    selectedDay = selected?.let { day ->
                        SelectedDay(
                            date = day,
                            goals = goals
                                .filter { !it.createdAt.atZone(zoneProvider.current()).toLocalDate().isAfter(day) }
                                .map { goal ->
                                    val key = periodCalculator.periodKey(goal.recurrence, day)
                                    DayGoal(
                                        goal = goal,
                                        periodKey = key,
                                        isCompleted = key in keysByGoal[goal.id].orEmpty(),
                                    )
                                },
                        )
                    },
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = HistoryUiState(),
        )

    fun selectDay(date: LocalDate) {
        selectedDate.value = date
    }

    fun dismissDay() {
        selectedDate.value = null
    }

    /**
     * Marks or unmarks one goal for the selected day.
     *
     * Ticking a weekly goal from a Wednesday cell marks the whole week — the key is
     * the period, not the day, which is the same rule the Today screen follows.
     */
    fun toggle(dayGoal: DayGoal) {
        viewModelScope.launch {
            completionRepository.setCompleted(
                goalId = dayGoal.goal.id,
                periodKey = dayGoal.periodKey,
                completed = !dayGoal.isCompleted,
                at = clock.instant(),
            )
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
