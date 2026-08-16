package com.checklisted.app.ui.today

import com.checklisted.app.domain.model.Completion
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.model.WeekStart
import com.checklisted.app.domain.period.TodayClock
import com.checklisted.app.domain.repository.CompletionRepository
import com.checklisted.app.domain.repository.GoalRepository
import com.checklisted.app.domain.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    /** 2026-08-16 is a Sunday: ISO week 33, month 08. */
    private val today = LocalDate.parse("2026-08-16")
    private val clock: Clock = Clock.fixed(Instant.parse("2026-08-16T12:00:00Z"), ZoneId.of("UTC"))

    private val goals = MutableStateFlow<List<Goal>>(emptyList())
    private val completions = MutableStateFlow<List<Completion>>(emptyList())
    private val weekStart = MutableStateFlow(WeekStart.MONDAY)
    private var savedOrder: List<String>? = null

    private val goalRepository = object : GoalRepository {
        override fun observeGoals(includeArchived: Boolean): Flow<List<Goal>> = goals

        override fun observeGoal(id: String): Flow<Goal?> = goals.map { list -> list.find { it.id == id } }

        override suspend fun createGoal(
            title: String,
            description: String?,
            recurrence: Recurrence,
            colorTag: String,
        ): String = UUID.randomUUID().toString()

        override suspend fun updateGoal(goal: Goal) = Unit

        override suspend fun setArchived(id: String, archived: Boolean) {
            goals.value = goals.value.filterNot { it.id == id }
        }

        override suspend fun deleteGoal(id: String) {
            goals.value = goals.value.filterNot { it.id == id }
        }

        override suspend fun reorderGoals(orderedIds: List<String>) {
            savedOrder = orderedIds
        }
    }

    private val completionRepository = object : CompletionRepository {
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
            completions.value = if (completed) {
                completions.value + Completion(UUID.randomUUID().toString(), goalId, periodKey, at)
            } else {
                completions.value.filterNot { it.goalId == goalId && it.periodKey == periodKey }
            }
        }
    }

    private val settingsRepository = object : SettingsRepository {
        override val weekStart: Flow<WeekStart> = this@TodayViewModelTest.weekStart

        override suspend fun setWeekStart(weekStart: WeekStart) {
            this@TodayViewModelTest.weekStart.value = weekStart
        }
    }

    /** Driven by the test so the calendar can be made to turn over on demand. */
    private val todayFlow = MutableStateFlow(today)
    private val todayClock = object : TodayClock {
        override val today: Flow<LocalDate> = todayFlow
    }

    private fun goal(
        id: String,
        recurrence: Recurrence,
        position: Int,
        title: String = id,
    ) = Goal(
        id = id,
        title = title,
        recurrence = recurrence,
        colorTag = "YELLOW",
        position = position,
        createdAt = Instant.parse("2026-01-01T00:00:00Z"),
    )

    private fun viewModel() = TodayViewModel(
        goalRepository = goalRepository,
        completionRepository = completionRepository,
        settingsRepository = settingsRepository,
        todayClock = todayClock,
        clock = clock,
    )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `goals are split into one section per recurrence`() = runTest(dispatcher) {
        goals.value = listOf(
            goal("a", Recurrence.DAILY, 0),
            goal("b", Recurrence.WEEKLY, 1),
            goal("c", Recurrence.MONTHLY, 2),
            goal("d", Recurrence.DAILY, 3),
        )

        val state = viewModel().uiState.first { !it.isLoading }

        assertEquals(listOf("a", "d"), state.sections[0].goals.map { it.goal.id })
        assertEquals(listOf("b"), state.sections[1].goals.map { it.goal.id })
        assertEquals(listOf("c"), state.sections[2].goals.map { it.goal.id })
    }

    @Test
    fun `each section asks about its own period key`() = runTest(dispatcher) {
        goals.value = listOf(
            goal("a", Recurrence.DAILY, 0),
            goal("b", Recurrence.WEEKLY, 1),
            goal("c", Recurrence.MONTHLY, 2),
        )

        val state = viewModel().uiState.first { !it.isLoading }

        assertEquals(PeriodKey("2026-08-16"), state.sections[0].periodKey)
        assertEquals(PeriodKey("2026-W33"), state.sections[1].periodKey)
        assertEquals(PeriodKey("2026-08"), state.sections[2].periodKey)
    }

    @Test
    fun `a completion only counts for the goal and period it belongs to`() = runTest(dispatcher) {
        goals.value = listOf(goal("a", Recurrence.DAILY, 0), goal("b", Recurrence.DAILY, 1))
        completions.value = listOf(
            Completion("1", "a", PeriodKey("2026-08-16"), Instant.now()),
            // Same goal, yesterday: must not tick today's row.
            Completion("2", "b", PeriodKey("2026-08-15"), Instant.now()),
        )

        val daily = viewModel().uiState.first { !it.isLoading }.sections[0]

        assertTrue(daily.goals.first { it.goal.id == "a" }.isCompleted)
        assertFalse(daily.goals.first { it.goal.id == "b" }.isCompleted)
        assertEquals(1, daily.completed)
        assertEquals(2, daily.total)
        assertEquals(0.5f, daily.progress, 0f)
    }

    @Test
    fun `changing the week start re-keys the weekly section`() = runTest(dispatcher) {
        // Monday 2026-08-17 is ISO week 34, but under Sunday-start weeks it still
        // belongs to the week that opened on Sunday the 16th, which is week 33.
        todayFlow.value = LocalDate.parse("2026-08-17")
        goals.value = listOf(goal("b", Recurrence.WEEKLY, 0))
        val viewModel = viewModel()

        assertEquals(PeriodKey("2026-W34"), viewModel.uiState.first { !it.isLoading }.sections[1].periodKey)

        weekStart.value = WeekStart.SUNDAY

        assertEquals(
            PeriodKey("2026-W33"),
            viewModel.uiState.first { it.sections[1].periodKey == PeriodKey("2026-W33") }.sections[1].periodKey,
        )
    }

    @Test
    fun `the day turning over re-keys the daily section without touching history`() = runTest(dispatcher) {
        goals.value = listOf(goal("a", Recurrence.DAILY, 0))
        val viewModel = viewModel()

        val before = viewModel.uiState.first { !it.isLoading }.sections[0]
        viewModel.toggle(before.goals.first())
        testScheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.first { !it.isLoading }.sections[0].goals.first().isCompleted)

        // Midnight passes.
        todayFlow.value = LocalDate.parse("2026-08-17")

        val after = viewModel.uiState.first { it.sections[0].periodKey == PeriodKey("2026-08-17") }
        // The row resets because it is asking about a new key, and yesterday's
        // completion is still on record.
        assertFalse(after.sections[0].goals.first().isCompleted)
        assertEquals(listOf(PeriodKey("2026-08-16")), completions.value.map { it.periodKey })
    }

    @Test
    fun `an empty state is only reported when there are no goals at all`() = runTest(dispatcher) {
        val emptyState = viewModel().uiState.first { !it.isLoading }
        assertTrue(emptyState.hasNoGoals)

        goals.value = listOf(goal("a", Recurrence.DAILY, 0))
        assertFalse(viewModel().uiState.first { !it.isLoading }.hasNoGoals)
    }

    // region reordering

    @Test
    fun `a goal moves within its own section`() = runTest(dispatcher) {
        goals.value = listOf(
            goal("a", Recurrence.DAILY, 0),
            goal("b", Recurrence.DAILY, 1),
            goal("c", Recurrence.DAILY, 2),
        )
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        assertTrue(viewModel.moveGoal(draggedId = "c", targetId = "a"))

        val state = viewModel.uiState.first { it.sections[0].goals.first().goal.id == "c" }
        assertEquals(listOf("c", "a", "b"), state.sections[0].goals.map { it.goal.id })
    }

    @Test
    fun `a goal cannot be dragged into another recurrence`() = runTest(dispatcher) {
        goals.value = listOf(goal("a", Recurrence.DAILY, 0), goal("b", Recurrence.WEEKLY, 1))
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        assertFalse(viewModel.moveGoal(draggedId = "a", targetId = "b"))

        val state = viewModel.uiState.first { !it.isLoading }
        assertEquals(listOf("a"), state.sections[0].goals.map { it.goal.id })
        assertEquals(listOf("b"), state.sections[1].goals.map { it.goal.id })
    }

    @Test
    fun `a lone goal in a section cannot be reordered`() = runTest(dispatcher) {
        goals.value = listOf(goal("a", Recurrence.DAILY, 0))
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        assertFalse(viewModel.moveGoal(draggedId = "a", targetId = "a"))
    }

    @Test
    fun `committing a drag writes the full order out`() = runTest(dispatcher) {
        goals.value = listOf(
            goal("a", Recurrence.DAILY, 0),
            goal("b", Recurrence.DAILY, 1),
            goal("w", Recurrence.WEEKLY, 2),
        )
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.moveGoal(draggedId = "b", targetId = "a")
        viewModel.commitOrder()
        testScheduler.advanceUntilIdle()

        // The whole list is written, not just the section that moved, so positions
        // stay globally consistent.
        assertEquals(listOf("b", "a", "w"), savedOrder)
    }

    @Test
    fun `cancelling a drag restores the database order`() = runTest(dispatcher) {
        goals.value = listOf(goal("a", Recurrence.DAILY, 0), goal("b", Recurrence.DAILY, 1))
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.moveGoal(draggedId = "b", targetId = "a")
        viewModel.uiState.first { it.sections[0].goals.first().goal.id == "b" }

        viewModel.cancelReorder()

        assertEquals(
            listOf("a", "b"),
            viewModel.uiState.first { it.sections[0].goals.first().goal.id == "a" }
                .sections[0].goals.map { it.goal.id },
        )
        assertEquals(null, savedOrder)
    }

    // endregion

    @Test
    fun `toggling writes a completion for the section's period`() = runTest(dispatcher) {
        goals.value = listOf(goal("b", Recurrence.WEEKLY, 0))
        val viewModel = viewModel()
        val status = viewModel.uiState.first { !it.isLoading }.sections[1].goals.first()

        viewModel.toggle(status)
        testScheduler.advanceUntilIdle()

        assertEquals(listOf(PeriodKey("2026-W33")), completions.value.map { it.periodKey })

        // Re-read rather than reusing the captured status: that one still says
        // "not completed", and toggling it again would insert a second row.
        val ticked = viewModel.uiState.first { it.sections[1].goals.first().isCompleted }
            .sections[1].goals.first()
        viewModel.toggle(ticked)
        testScheduler.advanceUntilIdle()

        assertTrue(completions.value.isEmpty())
    }
}
