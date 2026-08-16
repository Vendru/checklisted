package com.checklisted.app.domain.reminder

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

class ReminderScheduleTest {
    private val saoPaulo = ZoneId.of("America/Sao_Paulo")

    private fun delay(nowIso: String, at: String, zone: ZoneId = saoPaulo): Duration =
        ReminderSchedule.delayUntilNext(Instant.parse(nowIso), zone, LocalTime.parse(at))

    @Test
    fun `a time later today fires today`() {
        // 09:00 local (12:00Z) with a 21:00 reminder: twelve hours away.
        assertEquals(Duration.ofHours(12), delay("2026-08-16T12:00:00Z", "21:00"))
    }

    @Test
    fun `a time already past fires tomorrow`() {
        // 22:00 local with a 21:00 reminder: twenty-three hours away.
        assertEquals(Duration.ofHours(23), delay("2026-08-17T01:00:00Z", "21:00"))
    }

    @Test
    fun `the exact reminder instant schedules a full day out, not zero`() {
        // A zero delay would fire immediately and then loop.
        val delay = delay("2026-08-17T00:00:00Z", "21:00")

        assertEquals(Duration.ofHours(24), delay)
        assertTrue(delay > Duration.ZERO)
    }

    @Test
    fun `the delay is never negative`() {
        listOf("00:00", "06:30", "12:00", "23:59").forEach { at ->
            listOf("2026-08-16T00:00:00Z", "2026-08-16T12:00:00Z", "2026-08-16T23:59:00Z").forEach { now ->
                assertTrue("$now -> $at", delay(now, at) >= Duration.ZERO)
            }
        }
    }

    @Test
    fun `crossing into a day that loses an hour is still under 24 hours`() {
        // Lisbon springs forward on 2026-03-29: that local day is 23 hours long, so a
        // fixed 24-hour repeat would drift the reminder an hour later.
        val lisbon = ZoneId.of("Europe/Lisbon")
        val delay = delay("2026-03-28T21:00:00Z", "21:00", lisbon)

        assertEquals(Duration.ofHours(23), delay)
    }

    @Test
    fun `crossing into a day that gains an hour is still handled`() {
        // Lisbon falls back on 2026-10-25: that local day is 25 hours long.
        val lisbon = ZoneId.of("Europe/Lisbon")
        val delay = delay("2026-10-24T20:00:00Z", "21:00", lisbon)

        assertEquals(Duration.ofHours(25), delay)
    }

    @Test
    fun `a reminder set inside a skipped hour still resolves to a real instant`() {
        // Sao Paulo has no DST today, so this uses a zone that skips 02:00-03:00.
        val lisbon = ZoneId.of("Europe/Lisbon")
        val delay = ReminderSchedule.delayUntilNext(
            now = Instant.parse("2026-03-29T00:30:00Z"),
            zone = lisbon,
            time = LocalTime.parse("01:30"),
        )

        assertTrue(delay > Duration.ZERO)
        assertTrue(delay < Duration.ofHours(25))
    }

    @Test
    fun `midnight is a valid reminder time`() {
        assertEquals(Duration.ofHours(3), delay("2026-08-16T00:00:00Z", "00:00"))
    }
}
