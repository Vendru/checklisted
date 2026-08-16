package com.checklisted.app.ui.history

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val today = LocalDate.parse("2026-08-16")

    private val goalRepository = FakeGoalRepository()
    private val completionRepository = FakeCompletionRepository()
    private val clock: Clock = Clock.fixed(Instant.parse("2026-08-16T12:00:00Z"), ZoneId.of("UTC"))

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun goal(
        id: String,
        recurrence: Recurrence = Recurrence.DAILY,
        createdAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
        archived: Boolean = false,
    ) = Goal(
        id = id,
        title = id,
        recurrence = recurrence,
        colorTag = "YELLOW",
        position = 0,
        isArchived = archived,
        createdAt = createdAt,
    )

    private fun viewModel() = HistoryViewModel(
        completionRepository = completionRepository,
        goalRepository = goalRepository,
        settingsRepository = FakeSettingsRepository(),
        todayClock = FakeTodayClock(today),
        zoneProvider = FakeZoneProvider(),
        clock = clock,
    )

    @Test
    fun `a day's fraction is completed over goals that existed then`() = runTest(dispatcher) {
        goalRepository.goals.value = listOf(goal("a"), goal("b"), goal("c"))
        completionRepository.completions.value = listOf(
            Completion("1", "a", PeriodKey("2026-08-15"), Instant.EPOCH),
            Completion("2", "b", PeriodKey("2026-08-15"), Instant.EPOCH),
        )

        val day = viewModel().uiState.first { !it.isLoading }
            .heatmap.first { it.date == LocalDate.parse("2026-08-15") }

        assertEquals(2, day.completed)
        assertEquals(3, day.total)
    }

    @Test
    fun `archived goals stay in the grid`() = runTest(dispatcher) {
        goalRepository.goals.value = listOf(goal("a", archived = true))
        completionRepository.completions.value =
            listOf(Completion("1", "a", PeriodKey("2026-08-15"), Instant.EPOCH))

        val state = viewModel().uiState.first { !it.isLoading }
        val day = state.heatmap.first { it.date == LocalDate.parse("2026-08-15") }

        // Their history happened; hiding it would leave holes with no visible cause.
        assertEquals(1, day.completed)
        assertEquals(1, day.total)
        // But they do not count as goals the user currently has.
        assertEquals(0, state.goalCount)
    }

    @Test
    fun `opening a day lists the goals that existed on it`() = runTest(dispatcher) {
        goalRepository.goals.value = listOf(
            goal("old"),
            goal("new", createdAt = Instant.parse("2026-08-10T00:00:00Z")),
        )
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.selectDay(LocalDate.parse("2026-08-05"))

        val selected = viewModel.uiState.first { it.selectedDay != null }.selectedDay!!
        assertEquals(listOf("old"), selected.goals.map { it.goal.id })
    }

    @Test
    fun `a weekly goal opened from any weekday carries the week's key`() = runTest(dispatcher) {
        goalRepository.goals.value = listOf(goal("w", recurrence = Recurrence.WEEKLY))
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.selectDay(LocalDate.parse("2026-07-08"))
        val dayGoal = viewModel.uiState.first { it.selectedDay != null }.selectedDay!!.goals.single()

        assertEquals(PeriodKey("2026-W28"), dayGoal.periodKey)

        viewModel.toggle(dayGoal)
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf(PeriodKey("2026-W28")),
            completionRepository.completions.value.map { it.periodKey },
        )
    }

    @Test
    fun `toggling from the day sheet marks and unmarks`() = runTest(dispatcher) {
        goalRepository.goals.value = listOf(goal("a"))
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }
        viewModel.selectDay(LocalDate.parse("2026-08-05"))

        val before = viewModel.uiState.first { it.selectedDay != null }.selectedDay!!.goals.single()
        assertFalse(before.isCompleted)

        viewModel.toggle(before)
        testScheduler.advanceUntilIdle()

        val after = viewModel.uiState.first { it.selectedDay?.goals?.single()?.isCompleted == true }
            .selectedDay!!.goals.single()
        assertTrue(after.isCompleted)

        viewModel.toggle(after)
        testScheduler.advanceUntilIdle()

        assertTrue(completionRepository.completions.value.isEmpty())
    }

    @Test
    fun `dismissing clears the selection`() = runTest(dispatcher) {
        goalRepository.goals.value = listOf(goal("a"))
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.selectDay(today)
        viewModel.uiState.first { it.selectedDay != null }

        viewModel.dismissDay()
        assertNull(viewModel.uiState.first { it.selectedDay == null }.selectedDay)
    }
}
