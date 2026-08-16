package com.checklisted.app.domain.period

import java.time.ZoneId

/**
 * Supplies the timezone period keys are derived from.
 *
 * Resolved on every call rather than captured once: a user can cross a timezone
 * mid-session, and the day boundary has to follow them.
 */
fun interface ZoneProvider {
    fun current(): ZoneId
}
