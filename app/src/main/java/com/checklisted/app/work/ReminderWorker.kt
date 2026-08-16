package com.checklisted.app.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.checklisted.app.MainActivity
import com.checklisted.app.R
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.period.PeriodCalculator
import com.checklisted.app.domain.period.ZoneProvider
import com.checklisted.app.domain.repository.CompletionRepository
import com.checklisted.app.domain.repository.GoalRepository
import com.checklisted.app.domain.repository.SettingsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.Clock

/**
 * Posts the daily reminder, then books the next one.
 *
 * Self-rescheduling one-shot work rather than `PeriodicWorkRequest`: a periodic
 * request repeats every fixed 24 hours, which slides an hour off the user's chosen
 * time whenever the clocks change. Recomputing the next local occurrence after each
 * run keeps it pinned to the wall clock.
 */
@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val goalRepository: GoalRepository,
    private val completionRepository: CompletionRepository,
    private val settingsRepository: SettingsRepository,
    private val reminderScheduler: ReminderScheduler,
    private val zoneProvider: ZoneProvider,
    private val clock: Clock,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val settings = settingsRepository.settings.first()
        if (!settings.reminderEnabled) return Result.success()

        val pending = countPending(settings.weekStart)
        // Nothing left to do today: staying quiet is the whole point of counting.
        if (pending > 0) notify(pending)

        reminderScheduler.schedule(settings.reminderTime)
        return Result.success()
    }

    private suspend fun countPending(weekStart: com.checklisted.app.domain.model.WeekStart): Int {
        val periods = PeriodCalculator(weekStart)
        val today = periods.today(clock.instant(), zoneProvider.current())
        val keysByRecurrence = Recurrence.entries.associateWith { periods.periodKey(it, today) }

        val goals = goalRepository.observeGoals().first()
        val completions = completionRepository.observeCompletions(keysByRecurrence.values).first()
        val done = completions.mapTo(mutableSetOf()) { it.goalId to it.periodKey }

        return goals.count { goal -> (goal.id to keysByRecurrence.getValue(goal.recurrence)) !in done }
    }

    private fun notify(pending: Int) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        // On API 32 and below the permission does not exist and is reported granted.
        if (!granted) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.notification_channel_description)
            },
        )

        val intent = android.content.Intent(context, MainActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = android.app.PendingIntent.getActivity(
            context,
            0,
            intent,
            android.app.PendingIntent.FLAG_IMMUTABLE or android.app.PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(
                context.resources.getQuantityString(R.plurals.notification_pending, pending, pending),
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "checklisted-daily-reminder"

        private const val CHANNEL_ID = "daily_reminder"
        private const val NOTIFICATION_ID = 1
    }
}
