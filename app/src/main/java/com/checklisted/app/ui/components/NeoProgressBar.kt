package com.checklisted.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme

private val DefaultBarHeight = 22.dp

/**
 * Chunky progress bar for the per-section counters on the Today screen.
 *
 * Composed from [neoSurface] like every other component, so it sits on the same
 * plane as the cards around it. The fill is a plain rectangle that grows — no
 * rounded cap, no gradient.
 */
@Composable
fun NeoProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accent: NeoAccent = NeoAccent.Default,
    height: Dp = DefaultBarHeight,
    animated: Boolean = true,
) {
    val target = progress.coerceIn(0f, 1f)
    val shown = if (animated) {
        val value by animateFloatAsState(
            targetValue = target,
            animationSpec = tween(durationMillis = 180, easing = LinearEasing),
            label = "neoProgress",
        )
        value
    } else {
        target
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .neoSurface(color = NeoTheme.colors.surface, shape = NeoShapes.extraSmall)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(shown, 0f..1f) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(shown)
                .background(accent.color),
        )
    }
}

@NeoPreviews
@Composable
private fun NeoProgressBarPreview() {
    PreviewStack { NeoProgressBarSamples() }
}

@Composable
internal fun NeoProgressBarSamples() {
    NeoProgressBar(progress = 0f, animated = false)
    NeoProgressBar(progress = 0.35f, animated = false, accent = NeoAccent.TEAL)
    NeoProgressBar(progress = 0.6f, animated = false, accent = NeoAccent.PINK)
    NeoProgressBar(progress = 1f, animated = false, accent = NeoAccent.PURPLE, height = 28.dp)
}
