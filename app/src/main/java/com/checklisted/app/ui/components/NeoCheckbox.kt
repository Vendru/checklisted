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
import com.checklisted.app.ui.theme.NeoShapes
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
                    color = if (checked) accent.color else colors.surface,
                    shape = NeoShapes.small,
                    pressed = pressed,
                    enabled = enabled,
                )
                .size(size)
                .drawWithContent {
                    drawContent()
                    if (tickProgress <= 0f) return@drawWithContent

                    val w = this.size.width
                    val h = this.size.height
                    val start = Offset(w * 0.24f, h * 0.52f)
                    val elbow = Offset(w * 0.44f, h * 0.72f)
                    val end = Offset(w * 0.78f, h * 0.28f)

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
