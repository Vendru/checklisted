package com.checklisted.app.ui.goal

import androidx.lifecycle.SavedStateHandle
import com.checklisted.app.domain.model.Completion
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.ui.FakeCompletionRepository
import com.checklisted.app.ui.FakeGoalRepository
import com.checklisted.app.ui.FakeSettingsRepository
import com.checklisted.app.ui.FakeTodayClock
import com.checklisted.app.ui.FakeZoneProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class GoalDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val today = LocalDate.parse("2026-08-16")

    private val goalRepository = FakeGoalRepository()
    private val completionRepository = FakeCompletionRepository()
    private val settingsRepository = FakeSettingsRepository()
    private val todayClock = FakeTodayClock(today)
    private val clock: Clock = Clock.fixed(Instant.parse("2026-08-16T12:00:00Z"), ZoneId.of("UTC"))

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun seedGoal(
        recurrence: Recurrence = Recurrence.DAILY,
        createdAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
    ): Goal {
        val goal = Goal(
            id = "g1",
            title = "Correr",
            recurrence = recurrence,
            colorTag = "TEAL",
            position = 0,
            createdAt = createdAt,
        )
        goalRepository.goals.value = listOf(goal)
        return goal
    }

    private fun complete(vararg keys: String) {
        completionRepository.completions.value = keys.map {
            Completion(it, "g1", PeriodKey(it), Instant.parse("2026-08-01T00:00:00Z"))
        }
    }

    private fun viewModel() = GoalDetailViewModel(
        goalRepository = goalRepository,
        completionRepository = completionRepository,
        settingsRepository = settingsRepository,
        todayClock = todayClock,
        zoneProvider = FakeZoneProvider(),
        clock = clock,
        savedStateHandle = SavedStateHandle(mapOf("goalId" to "g1")),
    )

    @Test
    fun `stats follow the neutral open period rule`() = runTest(dispatcher) {
        seedGoal()
        complete("2026-08-15", "2026-08-14", "2026-08-13")

        val stats = viewModel().uiState.first { !it.isLoading }.stats!!

        // Today is untouched and does not break the run.
        assertEquals(3, stats.currentStreak)
        assertEquals(3, stats.bestStreak)
    }

    @Test
    fun `the rate window is clamped to the goal's lifetime`() = runTest(dispatcher) {
        seedGoal(createdAt = Instant.parse("2026-08-13T00:00:00Z"))
        complete("2026-08-15", "2026-08-14")

        val rate = viewModel().uiState.first { !it.isLoading }.stats!!.completionRate

        assertEquals(3, rate.total)
        assertEquals(2, rate.completed)
    }

    @Test
    fun `a missing goal is reported rather than left loading`() = runTest(dispatcher) {
        val state = viewModel().uiState.first { !it.isLoading }

        assertTrue(state.isMissing)
        assertEquals(null, state.goal)
    }

    @Test
    fun `the heatmap covers whole weeks up to today`() = runTest(dispatcher) {
        seedGoal()
        complete("2026-08-15")

        val heatmap = viewModel().uiState.first { !it.isLoading }.heatmap

        assertEquals(today, heatmap.last().date)
        assertEquals(1, heatmap.count { it.completed == 1 })
        assertEquals(LocalDate.parse("2026-08-15"), heatmap.first { it.completed == 1 }.date)
    }

    @Test
    fun `tapping a past day marks it retroactively`() = runTest(dispatcher) {
        seedGoal()
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.toggleDay(LocalDate.parse("2026-07-04"))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(PeriodKey("2026-07-04")), completionRepository.completions.value.map { it.periodKey })
    }

    @Test
    fun `tapping an already marked day clears it`() = runTest(dispatcher) {
        seedGoal()
        complete("2026-07-04")
        val viewModel = viewModel()
        viewModel.uiState.first { it.completedKeys.isNotEmpty() }

        viewModel.toggleDay(LocalDate.parse("2026-07-04"))
        testScheduler.advanceUntilIdle()

        assertTrue(completionRepository.completions.value.isEmpty())
    }

    @Test
    fun `tapping any day of a week marks the whole week for a weekly goal`() = runTest(dispatcher) {
        seedGoal(recurrence = Recurrence.WEEKLY)
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        // Wednesday 2026-07-08 belongs to week 28.
        viewModel.toggleDay(LocalDate.parse("2026-07-08"))
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(PeriodKey("2026-W28")), completionRepository.completions.value.map { it.periodKey })

        val heatmap = viewModel.uiState.first { it.completedKeys.isNotEmpty() }.heatmap
        val lit = heatmap.filter { it.completed == 1 }.map { it.date }
        assertEquals(7, lit.size)
        assertEquals(LocalDate.parse("2026-07-06"), lit.first())
        assertEquals(LocalDate.parse("2026-07-12"), lit.last())
    }

    @Test
    fun `days before the goal existed stay untracked in the heatmap`() = runTest(dispatcher) {
        seedGoal(createdAt = Instant.parse("2026-08-01T00:00:00Z"))

        val heatmap = viewModel().uiState.first { !it.isLoading }.heatmap

        assertFalse(heatmap.first().isTracked)
        assertTrue(heatmap.last().isTracked)
        assertTrue(heatmap.none { it.date < LocalDate.parse("2026-08-01") && it.isTracked })
    }
}
