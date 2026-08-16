package com.checklisted.app.domain.period

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Source of the user's current local date, re-emitting when it changes.
 *
 * An interface rather than a concrete class so the screens depend on the calendar
 * turning over, not on the Android broadcast plumbing that detects it — which also
 * means the view models are testable without a Context.
 */
interface TodayClock {
    val today: Flow<LocalDate>
}
