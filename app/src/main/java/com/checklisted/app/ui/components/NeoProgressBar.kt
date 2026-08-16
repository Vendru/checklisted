package com.checklisted.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

private val DefaultBarHeight = 22.dp

/**
 * Chunky progress bar for the per-section counters on the Today screen.
 *
 * Drawn as a bordered trough with a solid fill inside it. There is no rounded cap
 * and no gradient — the fill is a plain rectangle that grows.
 */
@Composable
fun NeoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accent: NeoAccent = NeoAccent.Default,
    height: Dp = DefaultBarHeight,
    animated: Boolean = true,
) {
    val colors = NeoTheme.colors
    val target = progress.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = if (animated) 180 else 0, easing = LinearEasing),
        label = "neoProgress",
    )
    val shown = if (animated) animatedProgress else target

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .border(width = NeoTokens.BorderWidth, color = colors.ink, shape = NeoShapes.extraSmall)
            .background(color = colors.surface, shape = NeoShapes.extraSmall)
            .padding(NeoTokens.BorderWidth)
            .clip(NeoShapes.extraSmall)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(shown, 0f..1f) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .widthFraction(shown)
                .background(accent.color),
        )
    }
}

/**
 * Constrains a child to [fraction] of the incoming max width.
 *
 * Used instead of `fillMaxWidth(fraction)` so a fraction of exactly 0 collapses to
 * zero width rather than snapping to the intrinsic minimum.
 */
private fun Modifier.widthFraction(fraction: Float): Modifier = layout { measurable, constraints ->
    val width = (constraints.maxWidth * fraction).toInt().coerceIn(0, constraints.maxWidth)
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(placeable.width, placeable.height) { placeable.place(0, 0) }
}

@Preview(name = "NeoProgressBar claro", showBackground = true, backgroundColor = 0xFFFAF3E0)
@Composable
private fun NeoProgressBarLightPreview() {
    NeoTheme(darkTheme = false) {
        PreviewStack { NeoProgressBarSamples() }
    }
}

@Preview(name = "NeoProgressBar escuro", showBackground = true, backgroundColor = 0xFF14120F)
@Composable
private fun NeoProgressBarDarkPreview() {
    NeoTheme(darkTheme = true) {
        PreviewStack { NeoProgressBarSamples() }
    }
}

@Composable
private fun NeoProgressBarSamples() {
    NeoProgressBar(progress = 0f, animated = false)
    NeoProgressBar(progress = 0.35f, animated = false, accent = NeoAccent.TEAL)
    NeoProgressBar(progress = 0.6f, animated = false, accent = NeoAccent.PINK)
    NeoProgressBar(progress = 1f, animated = false, accent = NeoAccent.PURPLE, height = 28.dp)
}
