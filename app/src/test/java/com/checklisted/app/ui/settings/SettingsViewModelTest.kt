package com.checklisted.app.ui.settings

import com.checklisted.app.domain.model.ThemeMode
import com.checklisted.app.domain.model.WeekStart
import com.checklisted.app.ui.FakeReminderPlanner
import com.checklisted.app.ui.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val settingsRepository = FakeSettingsRepository()
    private val reminderPlanner = FakeReminderPlanner()

    private fun viewModel() = SettingsViewModel(settingsRepository, reminderPlanner)

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `defaults are monday, system theme and no reminder`() = runTest(dispatcher) {
        val state = viewModel().uiState.first { !it.isLoading }

        assertEquals(WeekStart.MONDAY, state.settings.weekStart)
        assertEquals(ThemeMode.SYSTEM, state.settings.themeMode)
        assertEquals(false, state.settings.reminderEnabled)
        assertEquals(LocalTime.of(20, 0), state.settings.reminderTime)
    }

    @Test
    fun `changing the week start persists it`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.setWeekStart(WeekStart.SUNDAY)

        assertEquals(
            WeekStart.SUNDAY,
            viewModel.uiState.first { it.settings.weekStart == WeekStart.SUNDAY }.settings.weekStart,
        )
    }

    @Test
    fun `changing the theme persists it`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.setThemeMode(ThemeMode.DARK)

        assertEquals(
            ThemeMode.DARK,
            viewModel.uiState.first { it.settings.themeMode == ThemeMode.DARK }.settings.themeMode,
        )
    }

    @Test
    fun `changing the theme does not touch the reminder`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.setThemeMode(ThemeMode.DARK)
        viewModel.setWeekStart(WeekStart.SUNDAY)
        testScheduler.advanceUntilIdle()

        assertTrue(reminderPlanner.calls.isEmpty())
    }

    @Test
    fun `enabling the reminder books it at the stored time`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.setReminderEnabled(true)
        testScheduler.advanceUntilIdle()

        assertEquals(true to LocalTime.of(20, 0), reminderPlanner.last)
        assertTrue(viewModel.uiState.first { it.settings.reminderEnabled }.settings.reminderEnabled)
    }

    @Test
    fun `disabling the reminder cancels it`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }
        viewModel.setReminderEnabled(true)
        testScheduler.advanceUntilIdle()

        viewModel.setReminderEnabled(false)
        testScheduler.advanceUntilIdle()

        assertEquals(false to LocalTime.of(20, 0), reminderPlanner.last)
    }

    @Test
    fun `changing the time rebooks at the new time`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }
        viewModel.setReminderEnabled(true)
        testScheduler.advanceUntilIdle()

        viewModel.setReminderTime(LocalTime.of(8, 0))
        testScheduler.advanceUntilIdle()

        // Rebooked, not left at the old hour with a stale request behind it.
        assertEquals(true to LocalTime.of(8, 0), reminderPlanner.last)
        // Waits for the value rather than reading it: once the screen stops
        // collecting, the state flow replays its cached value before the upstream
        // restarts, so a bare first() would see the previous time.
        assertEquals(
            LocalTime.of(8, 0),
            viewModel.uiState.first { it.settings.reminderTime == LocalTime.of(8, 0) }.settings.reminderTime,
        )
    }

    @Test
    fun `changing the time while disabled does not book anything`() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.uiState.first { !it.isLoading }

        viewModel.setReminderTime(LocalTime.of(8, 0))
        testScheduler.advanceUntilIdle()

        assertEquals(false to LocalTime.of(8, 0), reminderPlanner.last)
    }
}
