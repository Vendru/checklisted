package com.checklisted.app.work

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.checklisted.app.domain.repository.SettingsRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Re-books the reminder after a reboot.
 *
 * WorkManager restores its own queue across restarts, but the reminder is a
 * self-rescheduling one-shot: if the device was off when it should have fired, there
 * is nothing left in the queue and nothing to reschedule from.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var settingsRepository: SettingsRepository

    @Inject lateinit var reminderScheduler: ReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val settings = settingsRepository.settings.first()
                reminderScheduler.apply(settings.reminderEnabled, settings.reminderTime)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
