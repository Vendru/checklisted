package com.checklisted.app.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.checklisted.app.domain.period.ZoneProvider
import com.checklisted.app.domain.reminder.ReminderPlanner
import com.checklisted.app.domain.reminder.ReminderSchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Clock
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Books the next daily reminder.
 *
 * Uses a uniquely named one-shot request with [ExistingWorkPolicy.REPLACE], so
 * changing the time or toggling the reminder off never leaves an older request
 * behind to fire at the previous hour.
 */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val zoneProvider: ZoneProvider,
    private val clock: Clock,
) : ReminderPlanner {
    fun schedule(time: LocalTime) {
        val delay = ReminderSchedule.delayUntilNext(clock.instant(), zoneProvider.current(), time)

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            ReminderWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(ReminderWorker.WORK_NAME)
    }

    /** Applies the current preference, so callers do not have to branch. */
    override fun apply(enabled: Boolean, time: LocalTime) {
        if (enabled) schedule(time) else cancel()
    }
}
