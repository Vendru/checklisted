package com.checklisted.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme

private val BoxSize = 32.dp
private val CheckStroke = 4.dp

/**
 * The checkbox the whole app revolves around.
 *
 * Pressing it fires haptic feedback and sinks the box into its own shadow (handled
 * by [neoSurface]); the check mark itself is drawn stroke-by-stroke so the tick
 * appears to be written rather than faded in.
 */
@Composable
fun NeoCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accent: NeoAccent = NeoAccent.Default,
    enabled: Boolean = true,
    size: Dp = BoxSize,
    contentDescription: String? = null,
) {
    val colors = NeoTheme.colors
    val haptics = LocalHapticFeedback.current
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()

    val tickProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 120, easing = LinearEasing),
        label = "neoCheckTick",
    )

    Box(
        modifier = modifier
            .neoSurface(
                color = if (checked) accent.color else colors.surface,
                shape = NeoShapes.small,
                pressed = pressed,
                enabled = enabled,
            )
            .neoClickable(
                interactionSource = interactionSource,
                enabled = enabled,
                role = Role.Checkbox,
                onClickLabel = contentDescription,
            ) {
                haptics.performHapticFeedback(
                    if (checked) HapticFeedbackType.TextHandleMove else HapticFeedbackType.LongPress,
                )
                onCheckedChange(!checked)
            }
            .size(size)
            .drawWithContent {
                drawContent()
                if (tickProgress <= 0f) return@drawWithContent

                val w = this.size.width
                val h = this.size.height
                val start = Offset(w * 0.24f, h * 0.52f)
                val elbow = Offset(w * 0.44f, h * 0.72f)
                val end = Offset(w * 0.78f, h * 0.28f)

                // Two segments drawn in sequence: the short down-stroke first, then
                // the long up-stroke, so the tick reads as being written by hand.
                val firstLeg = (tickProgress / 0.4f).coerceAtMost(1f)
                val secondLeg = ((tickProgress - 0.4f) / 0.6f).coerceIn(0f, 1f)

                val path = Path().apply {
                    moveTo(start.x, start.y)
                    lineTo(
                        start.x + (elbow.x - start.x) * firstLeg,
                        start.y + (elbow.y - start.y) * firstLeg,
                    )
                    if (secondLeg > 0f) {
                        lineTo(
                            elbow.x + (end.x - elbow.x) * secondLeg,
                            elbow.y + (end.y - elbow.y) * secondLeg,
                        )
                    }
                }

                drawPath(
                    path = path,
                    color = colors.onAccent,
                    style = Stroke(
                        width = CheckStroke.toPx(),
                        cap = StrokeCap.Square,
                        join = StrokeJoin.Miter,
                    ),
                )
            },
    )
}

@Preview(name = "NeoCheckbox claro", showBackground = true, backgroundColor = 0xFFFAF3E0)
@Composable
private fun NeoCheckboxLightPreview() {
    NeoTheme(darkTheme = false) {
        PreviewStack { NeoCheckboxSamples() }
    }
}

@Preview(name = "NeoCheckbox escuro", showBackground = true, backgroundColor = 0xFF14120F)
@Composable
private fun NeoCheckboxDarkPreview() {
    NeoTheme(darkTheme = true) {
        PreviewStack { NeoCheckboxSamples() }
    }
}

@Composable
private fun NeoCheckboxSamples() {
    NeoCheckbox(checked = false, onCheckedChange = {})
    NeoCheckbox(checked = true, onCheckedChange = {})
    NeoCheckbox(checked = true, onCheckedChange = {}, accent = NeoAccent.PINK)
    NeoCheckbox(checked = true, onCheckedChange = {}, accent = NeoAccent.TEAL, size = 44.dp)
}
