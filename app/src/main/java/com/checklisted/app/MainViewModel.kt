package com.checklisted.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checklisted.app.domain.model.ThemeMode
import com.checklisted.app.domain.reminder.ReminderPlanner
import com.checklisted.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    reminderPlanner: ReminderPlanner,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = settingsRepository.settings
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.Default)

    init {
        // The reminder is a self-rescheduling one-shot, so a run that never happened
        // — device off, work cancelled by the system — leaves nothing in the queue.
        // Re-booking on launch costs nothing and closes that gap.
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            reminderPlanner.apply(settings.reminderEnabled, settings.reminderTime)
        }
    }
}
