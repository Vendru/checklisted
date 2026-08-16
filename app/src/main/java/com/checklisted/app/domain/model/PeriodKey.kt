package com.checklisted.app.domain.model

/**
 * Identifies one occurrence of a recurring goal.
 *
 * The format depends on the recurrence and is always derived from the user's local
 * calendar, never from UTC:
 *
 * - `DAILY` — `2026-08-16`
 * - `WEEKLY` — `2026-W33` (week-based year, so late December can belong to the
 *   next year's week 1)
 * - `MONTHLY` — `2026-08`
 *
 * Completions are keyed by this value, which is what makes a period rollover a
 * pure read-side concern: when the day turns over, the app starts asking about a
 * new key and every past key stays exactly as it was.
 */
@JvmInline
value class PeriodKey(val value: String) {
    override fun toString(): String = value
}
