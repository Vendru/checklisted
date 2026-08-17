package com.checklisted.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoCombCell
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

private val BoxSize = 32.dp
private val CheckStroke = 4.dp

/**
 * The checkbox the whole app revolves around.
 *
 * Pressing it fires haptic feedback and sinks the box into its own shadow (handled
 * by [neoSurface]); the check mark itself is drawn stroke-by-stroke so the tick
 * appears to be written rather than faded in.
 *
 * The painted box stays at [size], but the touch target is expanded to at least
 * [NeoTokens.MinTouchTarget] by an outer box that carries the gesture — growing the
 * visual to 48.dp instead would wreck the density of a goal list.
 */
@Composable
fun NeoCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    accent: NeoAccent? = null,
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

    // Captured before the semantics block: inside it, a bare `contentDescription`
    // would resolve to the SemanticsPropertyReceiver's own property.
    val description = contentDescription
    val stateLabel = stringResource(
        if (checked) R.string.state_completed else R.string.state_not_completed,
    )

    val touchTarget = maxOf(size + NeoTokens.ShadowOffset, NeoTokens.MinTouchTarget)

    Box(
        modifier = modifier
            .size(touchTarget)
            .neoToggleable(
                value = checked,
                interactionSource = interactionSource,
                enabled = enabled,
            ) { nowChecked ->
                haptics.performHapticFeedback(
                    if (nowChecked) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove,
                )
                onCheckedChange(nowChecked)
            }
            .semantics {
                if (description != null) this.contentDescription = description
                stateDescription = stateLabel
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .neoSurface(
                    color = if (checked) (accent?.color ?: colors.action) else colors.surface,
                    shape = NeoCombCell,
                    pressed = pressed,
                    enabled = enabled,
                )
                .size(size)
                .drawWithContent {
                    drawContent()
                    if (tickProgress <= 0f) return@drawWithContent

                    val w = this.size.width
                    val h = this.size.height
                    // Pulled in from the old square's corners: a hexagon has no room
                    // at 0.24/0.78 on the diagonals, and the tick would have crossed
                    // the sloped edges.
                    val start = Offset(w * 0.26f, h * 0.52f)
                    val elbow = Offset(w * 0.44f, h * 0.70f)
                    val end = Offset(w * 0.74f, h * 0.32f)

                    // Two segments drawn in sequence: the short down-stroke first,
                    // then the long up-stroke, so the tick reads as being written.
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
                        // Ink on every fill, including a tag colour. The white tick a
                        // tagged box used to get measured 2.27:1 on yellow and 2.92:1
                        // on teal — under the 3:1 a mark like this needs. Ink clears it
                        // on all five, worst case 3.66:1 on purple.
                        color = colors.onAction,
                        style = Stroke(
                            width = CheckStroke.toPx(),
                            cap = StrokeCap.Square,
                            join = StrokeJoin.Miter,
                        ),
                    )
                },
        )
    }
}

@NeoPreviews
@Composable
private fun NeoCheckboxPreview() {
    PreviewStack { NeoCheckboxSamples() }
}

@Composable
internal fun NeoCheckboxSamples() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        NeoCheckbox(checked = false, onCheckedChange = {})
        NeoCheckbox(checked = true, onCheckedChange = {})
        NeoCheckbox(checked = true, onCheckedChange = {}, accent = NeoAccent.PINK)
        NeoCheckbox(checked = true, onCheckedChange = {}, accent = NeoAccent.TEAL, size = 44.dp)
        NeoCheckbox(checked = true, onCheckedChange = {}, enabled = false)
    }
}
