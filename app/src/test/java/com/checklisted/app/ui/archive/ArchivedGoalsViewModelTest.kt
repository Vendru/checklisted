package com.checklisted.app.ui.archive

import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.ui.FakeGoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class ArchivedGoalsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val goalRepository = FakeGoalRepository()
    private val goals get() = goalRepository.goals

    private fun goal(id: String, archived: Boolean) = Goal(
        id = id,
        title = id,
        recurrence = Recurrence.DAILY,
        colorTag = "TEAL",
        position = 0,
        isArchived = archived,
        createdAt = Instant.EPOCH,
    )

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `only archived goals are listed`() = runTest(dispatcher) {
        goals.value = listOf(goal("ativa", archived = false), goal("guardada", archived = true))

        val state = ArchivedGoalsViewModel(goalRepository).uiState.first { !it.isLoading }

        assertEquals(listOf("guardada"), state.goals.map { it.id })
    }

    /**
     * The loop this screen exists to close: before it, archiving removed a goal from
     * the only list that could reach the button that brings it back.
     */
    @Test
    fun `unarchiving returns the goal to the active list`() = runTest(dispatcher) {
        goals.value = listOf(goal("guardada", archived = true))
        val viewModel = ArchivedGoalsViewModel(goalRepository)
        viewModel.uiState.first { it.goals.isNotEmpty() }

        viewModel.unarchive("guardada")
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList<String>(), viewModel.uiState.first { it.goals.isEmpty() }.goals.map { it.id })
        assertEquals(
            listOf("guardada"),
            goalRepository.observeGoals(includeArchived = false).first().map { it.id },
        )
    }

    @Test
    fun `deleting removes the goal outright`() = runTest(dispatcher) {
        goals.value = listOf(goal("guardada", archived = true))
        val viewModel = ArchivedGoalsViewModel(goalRepository)
        viewModel.uiState.first { it.goals.isNotEmpty() }

        viewModel.delete("guardada")
        testScheduler.advanceUntilIdle()

        assertEquals(emptyList<Goal>(), goals.value)
    }
}
