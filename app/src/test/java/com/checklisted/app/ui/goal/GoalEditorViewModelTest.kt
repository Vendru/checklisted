package com.checklisted.app.ui.goal

import androidx.lifecycle.SavedStateHandle
import com.checklisted.app.domain.model.Completion
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.ui.FakeCompletionRepository
import com.checklisted.app.ui.FakeGoalRepository
import com.checklisted.app.ui.navigation.Destination
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
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class GoalEditorViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val goalRepository = FakeGoalRepository()
    private val completionRepository = FakeCompletionRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(goalId: String? = null) = GoalEditorViewModel(
        goalRepository = goalRepository,
        completionRepository = completionRepository,
        savedStateHandle = SavedStateHandle(
            if (goalId == null) emptyMap() else mapOf(Destination.GoalEditor.ARG_GOAL_ID to goalId),
        ),
    )

    private fun existingGoal(recurrence: Recurrence = Recurrence.WEEKLY) {
        goalRepository.goals.value = listOf(
            Goal(
                id = "g",
                title = "Correr 5 km",
                recurrence = recurrence,
                colorTag = "PURPLE",
                position = 0,
                createdAt = Instant.EPOCH,
            ),
        )
    }

    private fun withHistory(count: Int) {
        completionRepository.completions.value = (0 until count).map {
            Completion("c$it", "g", PeriodKey("2026-W$it"), Instant.EPOCH)
        }
    }

    @Test
    fun `editing loads the goal and remembers what it is stored as`() = runTest(dispatcher) {
        existingGoal()
        withHistory(3)

        val state = viewModel("g").uiState.first { !it.isLoading }

        assertEquals("Correr 5 km", state.title)
        assertEquals(Recurrence.WEEKLY, state.recurrence)
        assertEquals(Recurrence.WEEKLY, state.savedRecurrence)
        assertEquals(3, state.completionCount)
        assertFalse(state.recurrenceHidesHistory)
    }

    @Test
    fun `changing the recurrence of a goal with history raises the warning`() = runTest(dispatcher) {
        existingGoal(Recurrence.WEEKLY)
        withHistory(23)
        val viewModel = viewModel("g")
        viewModel.uiState.first { !it.isLoading }

        viewModel.onRecurrenceChange(Recurrence.MONTHLY)
        assertTrue(viewModel.uiState.value.recurrenceHidesHistory)

        // Back to what it is stored as: nothing would move, so nothing to warn about.
        viewModel.onRecurrenceChange(Recurrence.WEEKLY)
        assertFalse(viewModel.uiState.value.recurrenceHidesHistory)
    }

    /**
     * The common case is picking a recurrence right after creating the goal, when
     * there is nothing to lose sight of. Warning there would be noise.
     */
    @Test
    fun `a goal with no history changes recurrence silently`() = runTest(dispatcher) {
        existingGoal(Recurrence.DAILY)
        val viewModel = viewModel("g")
        viewModel.uiState.first { !it.isLoading }

        viewModel.onRecurrenceChange(Recurrence.MONTHLY)

        assertEquals(0, viewModel.uiState.value.completionCount)
        assertFalse(viewModel.uiState.value.recurrenceHidesHistory)
    }

    @Test
    fun `a brand new goal never warns`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.onRecurrenceChange(Recurrence.MONTHLY)

        assertFalse(viewModel.uiState.value.isEditing)
        assertFalse(viewModel.uiState.value.recurrenceHidesHistory)
    }
}
