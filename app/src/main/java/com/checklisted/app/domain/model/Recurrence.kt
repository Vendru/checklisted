package com.checklisted.app.domain.model

import java.time.DayOfWeek

/**
 * How often a goal comes back around.
 *
 * Persisted by [name], so entries must keep their identifiers stable across
 * releases.
 */
enum class Recurrence {
    DAILY,
    WEEKLY,
    MONTHLY,
}

/**
 * Which day a week is considered to start on.
 *
 * Changing this re-buckets weekly period keys: a completion recorded under a
 * Monday-start week may land in a differently numbered week once the setting
 * flips. Nothing is deleted — the history is re-read through the new boundary.
 */
enum class WeekStart(val dayOfWeek: DayOfWeek) {
    MONDAY(DayOfWeek.MONDAY),
    SUNDAY(DayOfWeek.SUNDAY),
    ;

    companion object {
        val Default = MONDAY

        fun fromName(name: String?): WeekStart = entries.firstOrNull { it.name == name } ?: Default
    }
}
