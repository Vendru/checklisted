package com.checklisted.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.checklisted.app.domain.model.ThemeMode
import com.checklisted.app.domain.model.WeekStart
import com.checklisted.app.domain.repository.Settings
import com.checklisted.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override val settings: Flow<Settings> = dataStore.data.map { prefs ->
        Settings(
            weekStart = WeekStart.fromName(prefs[WEEK_START]),
            themeMode = ThemeMode.fromName(prefs[THEME_MODE]),
            reminderEnabled = prefs[REMINDER_ENABLED] ?: false,
            // Stored as minutes past midnight rather than a formatted string: no
            // parsing to get wrong, and no locale to depend on.
            reminderTime = prefs[REMINDER_MINUTES]
                ?.let { LocalTime.ofSecondOfDay(it.toLong() * SECONDS_PER_MINUTE) }
                ?: Settings.DEFAULT_REMINDER_TIME,
        )
    }.distinctUntilChanged()

    override val weekStart: Flow<WeekStart> = settings.map { it.weekStart }.distinctUntilChanged()

    override suspend fun setWeekStart(weekStart: WeekStart) {
        dataStore.edit { it[WEEK_START] = weekStart.name }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { it[THEME_MODE] = themeMode.name }
    }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        dataStore.edit { it[REMINDER_ENABLED] = enabled }
    }

    override suspend fun setReminderTime(time: LocalTime) {
        dataStore.edit { it[REMINDER_MINUTES] = time.toSecondOfDay() / SECONDS_PER_MINUTE }
    }

    private companion object {
        const val SECONDS_PER_MINUTE = 60

        val WEEK_START = stringPreferencesKey("week_start")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
        val REMINDER_MINUTES = intPreferencesKey("reminder_minutes")
    }
}
