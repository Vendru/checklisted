package com.checklisted.app.domain.reminder

import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Works out when the daily reminder should next fire.
 *
 * Kept as a pure function so the awkward cases — the chosen time already gone for
 * today, and the clocks changing — are covered by plain JVM tests rather than
 * discovered by a user who stopped being reminded in late October.
 */
object ReminderSchedule {
    /**
     * Delay from [now] until the next occurrence of [time] in [zone].
     *
     * Resolved through [ZonedDateTime], so on the day the clocks go forward a time
     * that does not exist locally is pushed to the next valid instant rather than
     * silently landing an hour off.
     */
    fun delayUntilNext(now: Instant, zone: ZoneId, time: LocalTime): Duration =
        Duration.between(now, nextOccurrence(now, zone, time).toInstant())

    /**
     * The next moment [time] comes round in [zone].
     *
     * Shared with the settings screen, which tells the user when the reminder will
     * next go off. Two answers to "when does it fire" computed separately would drift,
     * and the screen saying one thing while the worker did another is worse than not
     * saying anything.
     */
    fun nextOccurrence(now: Instant, zone: ZoneId, time: LocalTime): ZonedDateTime {
        val local = now.atZone(zone)
        val todayAt = local.toLocalDate().atTime(time).atZone(zone)

        // `isAfter` rather than `!isBefore`: firing again at the exact same instant
        // would schedule a zero delay and run twice.
        return if (todayAt.toInstant().isAfter(now)) {
            todayAt
        } else {
            local.toLocalDate().plusDays(1).atTime(time).atZone(zone)
        }
    }
}
