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
import com.checklisted.app.ui.theme.NeoColors
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
 * week. Intensity is quantised into four solid fills; four steps are all a reader can
 * tell apart at this size.
 *
 * The ramp is deliberately not the action colour and takes no accent: this grid
 * repeats its colour eighty times on one screen, and painting it in the colour of
 * buttons made "this is tappable" and "this was a busy week" look identical.
 */
@Composable
fun NeoHeatmap(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
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

        Legend(colors = colors)
    }
}

@Composable
private fun HeatmapCell(
    day: HeatmapDay,
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
            .background(cellColor(day, colors), NeoShapes.extraSmall)
            .border(NeoTokens.HairlineBorder, colors.divider, NeoShapes.extraSmall)
            .then(clickModifier)
            .semantics { contentDescription = label },
    )
}

/**
 * Four steps: untouched, and three levels of done.
 *
 * Untracked days — before the goal existed — get the same empty fill as a day with
 * nothing done. They are distinguishable by position, not by colour; a fifth shade
 * would be indistinguishable at this size.
 */
private fun cellColor(day: HeatmapDay, colors: NeoColors): Color = when {
    !day.isTracked || day.completed == 0 -> colors.dataLow
    day.fraction >= 1f -> colors.dataHigh
    day.fraction >= 0.5f -> lerp(colors.dataLow, colors.dataHigh, 0.7f)
    else -> lerp(colors.dataLow, colors.dataHigh, 0.4f)
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
                color = colors.inkSoft,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun Legend(colors: NeoColors) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            colors.dataLow,
            lerp(colors.dataLow, colors.dataHigh, 0.4f),
            lerp(colors.dataLow, colors.dataHigh, 0.7f),
            colors.dataHigh,
        ).forEach { fill ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(fill, NeoShapes.extraSmall)
                    .border(NeoTokens.HairlineBorder, colors.divider, NeoShapes.extraSmall),
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
        NeoHeatmap(days = days)
    }
}
