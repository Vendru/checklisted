package com.checklisted.app.data.period

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.checklisted.app.domain.period.PeriodCalculator
import com.checklisted.app.domain.period.TodayClock
import com.checklisted.app.domain.period.ZoneProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Emits the user's current local date, and emits again the moment it changes.
 *
 * Three independent triggers, because no single one is dependable:
 *
 * 1. A timer that sleeps until the next local midnight. This is what makes the list
 *    refresh itself while the app sits open across the day boundary.
 * 2. `ACTION_DATE_CHANGED` / `ACTION_TIME_CHANGED` / `ACTION_TIMEZONE_CHANGED`,
 *    which cover the clock being set by hand or the user crossing a timezone.
 * 3. Re-collection. Callers collect with `collectAsStateWithLifecycle`, so returning
 *    to the foreground restarts this flow and re-reads the date — the revalidation
 *    that matters most, since neither the timer nor a broadcast is guaranteed to run
 *    while the process is dozing.
 */
@Singleton
class PeriodClock @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clock: Clock,
    private val zoneProvider: ZoneProvider,
) : TodayClock {
    private val periods = PeriodCalculator()

    override val today: Flow<LocalDate> = merge(midnightTicks(), systemTimeChanges())
        .map { currentDate() }
        .distinctUntilChanged()

    private fun currentDate(): LocalDate = periods.today(clock.instant(), zoneProvider.current())

    /**
     * Emits immediately, then once per period boundary.
     *
     * The wait is recomputed from local midnight on every pass rather than fixed at
     * 24 hours, so a DST shift does not drag the boundary off by an hour.
     */
    private fun midnightTicks(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            val zone = zoneProvider.current()
            val now = clock.instant()
            val rollover = periods.nextDayRolloverAt(periods.today(now, zone), zone)
            delay((rollover.toEpochMilli() - now.toEpochMilli()).coerceAtLeast(MIN_WAIT_MS))
        }
    }

    private fun systemTimeChanges(): Flow<Unit> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(Unit)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        awaitClose { context.unregisterReceiver(receiver) }
    }

    private companion object {
        /** Guards against a zero or negative wait spinning the loop. */
        const val MIN_WAIT_MS = 1_000L
    }
}
