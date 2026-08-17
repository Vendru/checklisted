package com.checklisted.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme

/**
 * Illustrated empty state.
 *
 * The drawing is an empty checkbox at rest — the same geometry as [NeoCheckbox],
 * scaled up, so the blank screen is showing the user the thing they are about to
 * create rather than a generic shrug.
 */
@Composable
fun NeoEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = NeoTheme.colors

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        EmptyBoxMark()

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        action?.invoke()
    }
}

/**
 * An unfilled box on the page's own paper.
 *
 * It used to be 88.dp of solid accent, which made the heaviest block on an empty
 * screen the illustration rather than the button that gets the user out of it. The
 * tick carries the action colour instead — the same 3:1 it needs anywhere else, which
 * a filled tag colour would not clear on a white card.
 */
@Composable
private fun EmptyBoxMark() {
    val colors = NeoTheme.colors
    Column(
        modifier = Modifier
            .neoSurface(color = colors.surface, shape = NeoShapes.medium)
            .size(80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            // The same tick geometry NeoCheckbox draws, with the long leg left as
            // dashes: the box is half-drawn, waiting to be finished.
            val w = size.width
            val h = size.height
            val stroke = MarkStroke.toPx()
            val start = Offset(w * 0.20f, h * 0.50f)
            val elbow = Offset(w * 0.42f, h * 0.74f)
            val end = Offset(w * 0.82f, h * 0.24f)

            drawLine(colors.action, start, elbow, stroke, StrokeCap.Square)

            fun along(t: Float) = Offset(
                x = elbow.x + (end.x - elbow.x) * t,
                y = elbow.y + (end.y - elbow.y) * t,
            )
            listOf(0.04f to 0.24f, 0.44f to 0.64f, 0.84f to 1f).forEach { (from, to) ->
                drawLine(colors.action, along(from), along(to), stroke, StrokeCap.Square)
            }
        }
    }
}

/** Heavier than a border: at this size the design system's 1.5.dp reads as a hairline. */
private val MarkStroke = 3.dp

@NeoPreviews
@Composable
private fun NeoEmptyStatePreview() {
    PreviewStack {
        NeoEmptyState(
            title = stringResource(R.string.empty_today_title),
            message = stringResource(R.string.empty_today_message),
            action = { NeoButton(text = stringResource(R.string.action_new_goal), onClick = {}) },
        )
    }
}
