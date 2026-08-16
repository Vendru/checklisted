package com.checklisted.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.checklisted.app.domain.history.HeatmapDay
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val DAYS_PER_WEEK = 7

/**
 * Calendar grid of the recent past, one square per day.
 *
 * Weeks run down the page and weekdays across it, so the columns line up by day of
 * week. Intensity is quantised into four solid fills rather than being interpolated
 * continuously — a smooth ramp would read as a gradient, which this design forbids,
 * and four steps are all a reader can tell apart at this size anyway.
 */
@Composable
fun NeoHeatmap(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
    accent: NeoAccent = NeoAccent.Default,
    onDayClick: ((HeatmapDay) -> Unit)? = null,
) {
    val colors = NeoTheme.colors
    if (days.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        WeekdayHeader(firstDate = days.first().date)

        days.chunked(DAYS_PER_WEEK).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                week.forEach { day ->
                    HeatmapCell(
                        day = day,
                        accent = accent,
                        onClick = onDayClick,
                        modifier = Modifier.weight(1f),
                    )
                }
                // Keeps a short final week aligned to the left instead of stretching.
                repeat(DAYS_PER_WEEK - week.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }

        Legend(accent = accent, ink = colors.ink)
    }
}

@Composable
private fun HeatmapCell(
    day: HeatmapDay,
    accent: NeoAccent,
    onClick: ((HeatmapDay) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = NeoTheme.colors
    val interactionSource = rememberNeoInteractionSource()
    val label = "${day.date}: ${day.completed}/${day.total}"

    val clickModifier = if (onClick != null && day.isTracked) {
        Modifier.neoClickable(interactionSource = interactionSource) { onClick(day) }
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(cellColor(day, accent, colors.surface), NeoShapes.extraSmall)
            .border(NeoTokens.HairlineBorder, colors.ink, NeoShapes.extraSmall)
            .then(clickModifier)
            .semantics { contentDescription = label },
    )
}

/**
 * Four steps: untouched, and three levels of done.
 *
 * Untracked days — before the goal existed — get the plain surface, the same as a
 * day with nothing done. They are distinguishable by position, not by colour; a
 * fifth shade would be indistinguishable at 16.dp.
 */
private fun cellColor(day: HeatmapDay, accent: NeoAccent, surface: Color): Color = when {
    !day.isTracked || day.completed == 0 -> surface
    day.fraction >= 1f -> accent.color
    day.fraction >= 0.5f -> lerp(surface, accent.color, 0.7f)
    else -> lerp(surface, accent.color, 0.4f)
}

@Composable
private fun WeekdayHeader(firstDate: LocalDate) {
    val colors = NeoTheme.colors
    val locale = Locale.getDefault()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(DAYS_PER_WEEK) { index ->
            val day: DayOfWeek = firstDate.plusDays(index.toLong()).dayOfWeek
            Text(
                text = day.getDisplayName(TextStyle.NARROW, locale).uppercase(locale),
                style = MaterialTheme.typography.labelSmall,
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Legend(accent: NeoAccent, ink: Color) {
    val colors = NeoTheme.colors
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            colors.surface,
            lerp(colors.surface, accent.color, 0.4f),
            lerp(colors.surface, accent.color, 0.7f),
            accent.color,
        ).forEach { fill ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(fill, NeoShapes.extraSmall)
                    .border(NeoTokens.HairlineBorder, ink, NeoShapes.extraSmall),
            )
        }
    }
}

@NeoPreviews
@Composable
private fun NeoHeatmapPreview() {
    val start = LocalDate.parse("2026-06-01")
    val days = (0 until 63).map { offset ->
        HeatmapDay(
            date = start.plusDays(offset.toLong()),
            completed = when (offset % 5) {
                0 -> 0
                1 -> 1
                2 -> 2
                else -> 3
            },
            total = if (offset < 7) 0 else 3,
        )
    }
    PreviewStack {
        NeoHeatmap(days = days, accent = NeoAccent.TEAL)
    }
}
