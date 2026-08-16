package com.checklisted.app.domain.reminder

import java.time.LocalTime

/**
 * Books or cancels the daily reminder.
 *
 * An interface so the screens depend on "the reminder should now be at 20:00", not
 * on WorkManager — which also keeps the view models constructible without a Context.
 */
interface ReminderPlanner {
    fun apply(enabled: Boolean, time: LocalTime)
}
