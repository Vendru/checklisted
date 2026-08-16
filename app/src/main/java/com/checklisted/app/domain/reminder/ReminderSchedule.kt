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
    fun delayUntilNext(now: Instant, zone: ZoneId, time: LocalTime): Duration {
        val local = now.atZone(zone)
        val todayAt = local.toLocalDate().atTime(time).atZone(zone)

        // `!isAfter` rather than `isBefore`: firing again at the exact same instant
        // would schedule a zero delay and run twice.
        val next = if (todayAt.toInstant().isAfter(now)) {
            todayAt
        } else {
            local.toLocalDate().plusDays(1).atTime(time).atZone(zone)
        }

        return Duration.between(now, next.toInstant())
    }
}
