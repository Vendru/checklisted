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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens
import com.checklisted.app.ui.theme.displayUppercase

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
    accent: NeoAccent = NeoAccent.Default,
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
        EmptyBoxMark(accent = accent)

        Text(
            text = title.displayUppercase(),
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

@Composable
private fun EmptyBoxMark(accent: NeoAccent) {
    val colors = NeoTheme.colors
    Column(
        modifier = Modifier
            .neoSurface(color = accent.color, shape = NeoShapes.medium)
            .size(88.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(modifier = Modifier.size(44.dp)) {
            // A dashed, unfinished tick: the box is waiting to be filled in.
            val w = size.width
            val h = size.height
            val stroke = NeoTokens.BorderWidth.toPx()
            val tick = Path().apply {
                moveTo(w * 0.18f, h * 0.52f)
                lineTo(w * 0.42f, h * 0.76f)
            }
            drawPath(
                path = tick,
                color = colors.onAccent,
                style = Stroke(stroke, cap = StrokeCap.Square, join = StrokeJoin.Miter),
            )
            listOf(0.55f, 0.72f, 0.89f).forEach { fraction ->
                drawLine(
                    color = colors.onAccent,
                    start = Offset(w * fraction, h * (0.76f - (fraction - 0.42f))),
                    end = Offset(w * (fraction + 0.07f), h * (0.69f - (fraction - 0.42f))),
                    strokeWidth = stroke,
                    cap = StrokeCap.Square,
                )
            }
        }
    }
}

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
