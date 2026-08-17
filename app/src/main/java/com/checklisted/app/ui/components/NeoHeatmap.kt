package com.checklisted.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.domain.history.HeatmapDay
import com.checklisted.app.ui.AppLocale
import com.checklisted.app.ui.theme.NeoColors
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private const val DAYS_PER_WEEK = 7
private val LabelWidth = 20.dp
private val CellGap = 3.dp

/**
 * Calendar grid of the recent past, one square per day.
 *
 * Weekdays run down the page and **weeks run across it**, so three months fit in
 * seven short rows instead of thirteen tall ones. Laying it out the other way round
 * gave cells a seventh of the screen width each, made the grid taller than the
 * display, and — because the oldest period sorts first — opened the screen on months
 * of empty squares from before the goal existed. Here the newest week is the
 * rightmost column, which is where the eye lands.
 *
 * Intensity is quantised into four solid fills. The ramp is deliberately not the
 * action colour and takes no accent: this grid repeats its colour dozens of times on
 * one screen, and painting it in the colour of buttons made "this is tappable" and
 * "this was a busy week" look identical.
 */
@Composable
fun NeoHeatmap(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier,
    onDayClick: ((HeatmapDay) -> Unit)? = null,
) {
    val colors = NeoTheme.colors
    if (days.isEmpty()) return

    val locale = AppLocale
    val weeks = (days.size + DAYS_PER_WEEK - 1) / DAYS_PER_WEEK
    val firstDate = days.first().date

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(CellGap),
    ) {
        MonthAxis(days = days, weeks = weeks, locale = locale)

        repeat(DAYS_PER_WEEK) { weekday ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CellGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = firstDate.plusDays(weekday.toLong()).dayOfWeek
                        .getDisplayName(TextStyle.NARROW, locale)
                        .uppercase(locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSoft,
                    modifier = Modifier.width(LabelWidth),
                )
                repeat(weeks) { week ->
                    val day = days.getOrNull(week * DAYS_PER_WEEK + weekday)
                    if (day == null) {
                        // The rest of the current week. The record stops at today, so
                        // on a Monday the last column holds one single day: an invisible
                        // spacer here left the top row jutting a whole cell past every
                        // row below it, which reads as a broken grid rather than as a
                        // week that has not happened yet.
                        UpcomingCell(modifier = Modifier.weight(1f))
                    } else {
                        HeatmapCell(
                            day = day,
                            onClick = onDayClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        Legend(colors = colors)
    }
}

/**
 * Names the month over the column where it starts.
 *
 * Without it the grid is thirteen anonymous columns: the user can see that a week was
 * good but not which week it was. A month is written once, above its first column,
 * and the columns that continue it are left blank.
 */
@Composable
private fun MonthAxis(days: List<HeatmapDay>, weeks: Int, locale: Locale) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CellGap),
        verticalAlignment = Alignment.Bottom,
    ) {
        Spacer(modifier = Modifier.width(LabelWidth))
        var previousMonth: Month? = null
        repeat(weeks) { week ->
            val month = days.getOrNull(week * DAYS_PER_WEEK)?.date?.month
            val label = if (month != null && month != previousMonth) {
                month.getDisplayName(TextStyle.SHORT, locale).take(3)
            } else {
                ""
            }
            if (month != null) previousMonth = month

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = NeoTheme.colors.inkSoft,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                softWrap = false,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * A day that has not arrived yet.
 *
 * Same square as an empty day so the grid keeps its edges, but it carries no date, no
 * tap and no description: there is nothing to mark and nothing to announce.
 */
@Composable
private fun UpcomingCell(modifier: Modifier = Modifier) {
    val colors = NeoTheme.colors
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(colors.dataLow, NeoShapes.extraSmall)
            .border(NeoTokens.HairlineBorder, colors.divider, NeoShapes.extraSmall),
    )
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
private fun Legend(colors: NeoColors) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(LabelWidth))
        // Four unlabelled squares read as decoration. Naming the ends says they are a
        // scale and which way it runs.
        Text(
            text = stringResource(R.string.heatmap_legend_less),
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSoft,
        )
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
        Text(
            text = stringResource(R.string.heatmap_legend_more),
            style = MaterialTheme.typography.labelSmall,
            color = colors.inkSoft,
        )
    }
}

@NeoPreviews
@Composable
private fun NeoHeatmapPreview() {
    val start = LocalDate.parse("2026-06-01")
    val days = (0 until 91).map { offset ->
        HeatmapDay(
            date = start.plusDays(offset.toLong()),
            completed = when (offset % 5) {
                0 -> 0
                1 -> 1
                2 -> 2
                else -> 3
            },
            total = if (offset < 12) 0 else 3,
        )
    }
    PreviewStack {
        NeoHeatmap(days = days)
    }
}
