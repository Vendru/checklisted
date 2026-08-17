package com.checklisted.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checklisted.app.domain.model.ThemeMode
import com.checklisted.app.domain.model.WeekStart
import com.checklisted.app.domain.period.ZoneProvider
import com.checklisted.app.domain.reminder.ReminderPlanner
import com.checklisted.app.domain.reminder.ReminderSchedule
import com.checklisted.app.domain.repository.Settings
import com.checklisted.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalTime
import javax.inject.Inject

/**
 * When the reminder next goes off.
 *
 * Resolved here rather than in the screen because only this layer holds the injected
 * clock — a `LocalDate.now()` in a composable would be the one place in the app
 * reading the wall clock straight off the system, and untestable with it.
 */
data class NextReminder(val time: LocalTime, val isToday: Boolean)

data class SettingsUiState(
    val settings: Settings = Settings(),
    /** Null when the reminder is switched off. */
    val nextReminder: NextReminder? = null,
    val isLoading: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val reminderPlanner: ReminderPlanner,
    private val zoneProvider: ZoneProvider,
    private val clock: Clock,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = settingsRepository.settings
        .map { settings ->
            SettingsUiState(
                settings = settings,
                nextReminder = if (settings.reminderEnabled) {
                    val now = clock.instant()
                    val zone = zoneProvider.current()
                    val next = ReminderSchedule.nextOccurrence(now, zone, settings.reminderTime)
                    NextReminder(
                        time = next.toLocalTime(),
                        isToday = next.toLocalDate() == now.atZone(zone).toLocalDate(),
                    )
                } else {
                    null
                },
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            initialValue = SettingsUiState(),
        )

    fun setWeekStart(weekStart: WeekStart) {
        viewModelScope.launch { settingsRepository.setWeekStart(weekStart) }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(themeMode) }
    }

    /**
     * Persists the toggle and immediately books or cancels the work.
     *
     * Scheduling lives here rather than in a listener on the settings flow: the user
     * flipping the switch is the one moment the reminder should be (re)booked, and
     * reacting to the stored value would also re-book on every unrelated read.
     */
    fun setReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setReminderEnabled(enabled)
            val time = settingsRepository.settings.first().reminderTime
            reminderPlanner.apply(enabled, time)
        }
    }

    fun setReminderTime(time: LocalTime) {
        viewModelScope.launch {
            settingsRepository.setReminderTime(time)
            val settings = settingsRepository.settings.first()
            reminderPlanner.apply(settings.reminderEnabled, time)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
