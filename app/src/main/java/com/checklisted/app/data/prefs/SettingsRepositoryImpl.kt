package com.checklisted.app.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.checklisted.app.domain.model.WeekStart
import com.checklisted.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {
    override val weekStart: Flow<WeekStart> =
        dataStore.data.map { prefs -> WeekStart.fromName(prefs[WEEK_START]) }

    override suspend fun setWeekStart(weekStart: WeekStart) {
        dataStore.edit { prefs -> prefs[WEEK_START] = weekStart.name }
    }

    private companion object {
        val WEEK_START = stringPreferencesKey("week_start")
    }
}
