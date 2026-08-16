package com.checklisted.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.checklisted.app.data.local.ChecklistedDatabase
import com.checklisted.app.data.local.CompletionDao
import com.checklisted.app.data.local.GoalDao
import com.checklisted.app.data.period.PeriodClock
import com.checklisted.app.data.prefs.SettingsRepositoryImpl
import com.checklisted.app.data.repository.CompletionRepositoryImpl
import com.checklisted.app.data.repository.GoalRepositoryImpl
import com.checklisted.app.domain.period.TodayClock
import com.checklisted.app.domain.period.ZoneProvider
import com.checklisted.app.domain.repository.CompletionRepository
import com.checklisted.app.domain.repository.GoalRepository
import com.checklisted.app.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.time.Clock
import java.time.ZoneId
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ChecklistedDatabase =
        Room.databaseBuilder(context, ChecklistedDatabase::class.java, ChecklistedDatabase.NAME).build()

    @Provides
    fun provideGoalDao(database: ChecklistedDatabase): GoalDao = database.goalDao()

    @Provides
    fun provideCompletionDao(database: ChecklistedDatabase): CompletionDao = database.completionDao()

    @Provides
    @Singleton
    fun provideSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        context.settingsDataStore

    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Injected rather than reached for statically so period logic can be driven from
     * a fixed instant in tests.
     */
    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    fun provideZoneProvider(): ZoneProvider = ZoneProvider { ZoneId.systemDefault() }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    abstract fun bindCompletionRepository(impl: CompletionRepositoryImpl): CompletionRepository

    @Binds
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    abstract fun bindTodayClock(impl: PeriodClock): TodayClock
}
