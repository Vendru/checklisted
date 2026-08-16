package com.checklisted.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.theme.NeoTheme

/**
 * Icons drawn in code rather than imported.
 *
 * Material's icon set is uniformly thin-stroked, which reads as a different design
 * language next to 3.dp borders. These are square-capped, mitre-joined and heavy.
 */
private val DefaultIconSize = 22.dp
private val DefaultStroke = 3.dp

@Composable
fun NeoIconPlus(
    modifier: Modifier = Modifier,
    tint: Color = NeoTheme.colors.ink,
    size: Dp = DefaultIconSize,
) {
    NeoIcon(modifier, size) {
        val mid = this.size.width / 2
        val stroke = DefaultStroke.toPx()
        drawLine(tint, Offset(mid, 0f), Offset(mid, this.size.height), stroke, StrokeCap.Square)
        drawLine(tint, Offset(0f, mid), Offset(this.size.width, mid), stroke, StrokeCap.Square)
    }
}

@Composable
fun NeoIconBack(
    modifier: Modifier = Modifier,
    tint: Color = NeoTheme.colors.ink,
    size: Dp = DefaultIconSize,
) {
    NeoIcon(modifier, size) {
        val w = this.size.width
        val h = this.size.height
        val chevron = Path().apply {
            moveTo(w * 0.65f, h * 0.12f)
            lineTo(w * 0.25f, h * 0.5f)
            lineTo(w * 0.65f, h * 0.88f)
        }
        drawPath(
            path = chevron,
            color = tint,
            style = Stroke(DefaultStroke.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter),
        )
    }
}

/** Three stacked bars: the grab affordance on a goal row. */
@Composable
fun NeoIconDrag(
    modifier: Modifier = Modifier,
    tint: Color = NeoTheme.colors.ink,
    size: Dp = DefaultIconSize,
) {
    NeoIcon(modifier, size) {
        val w = this.size.width
        val h = this.size.height
        val stroke = DefaultStroke.toPx() * 0.8f
        listOf(0.32f, 0.5f, 0.68f).forEach { fraction ->
            drawLine(tint, Offset(w * 0.15f, h * fraction), Offset(w * 0.85f, h * fraction), stroke, StrokeCap.Square)
        }
    }
}

@Composable
private fun NeoIcon(modifier: Modifier, size: Dp, draw: DrawScope.() -> Unit) {
    Canvas(modifier = modifier.size(size)) { draw() }
}

@NeoPreviews
@Composable
private fun NeoIconsPreview() {
    PreviewStack {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            NeoIconPlus()
            NeoIconBack()
            NeoIconDrag()
        }
    }
}
