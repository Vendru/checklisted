package com.checklisted.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

/**
 * Custom [Indication] that renders nothing.
 *
 * The press treatment in this design system is structural — the component travels
 * into the space its shadow occupied — and is produced by [neoSurface], which owns
 * both the border and the shadow. This exists purely to displace the Material
 * ripple, which the design forbids.
 */
object NeoNoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = Node()

    private class Node : Modifier.Node(), DrawModifierNode {
        override fun ContentDrawScope.draw() = drawContent()
    }

    override fun hashCode(): Int = "NeoNoIndication".hashCode()

    override fun equals(other: Any?): Boolean = other === this
}

/**
 * Paints the neobrutalist body of a component: a solid offset shadow with no blur,
 * a flat fill, and a heavy border.
 *
 * While [pressed], the body travels [shadowOffset] in both axes and the shadow is
 * consumed by exactly as much, so the component reads as sinking onto its own
 * shadow. The travel runs on a 40 ms linear tween — this style has no room for
 * soft easing.
 *
 * The travel happens inside bounds reserved by the modifier itself, so pressing one
 * component never nudges its neighbours.
 */
@Composable
fun Modifier.neoSurface(
    color: Color,
    shape: Shape,
    pressed: Boolean = false,
    enabled: Boolean = true,
    borderColor: Color = NeoTheme.colors.ink,
    shadowColor: Color = NeoTheme.colors.shadow,
    borderWidth: Dp = NeoTokens.BorderWidth,
    shadowOffset: Dp = NeoTokens.ShadowOffset,
): Modifier {
    val travel by animateDpAsState(
        targetValue = if (pressed && enabled) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = 40, easing = LinearEasing),
        label = "neoPressTravel",
    )
    return this
        .reserveShadowGutter(shadowOffset)
        .travelBy(travel)
        .drawBehind {
            val remaining = shadowOffset.toPx() - travel.toPx()
            if (remaining > 0f) {
                val outline = shape.createOutline(size, layoutDirection, this)
                translate(left = remaining, top = remaining) {
                    drawOutline(outline, shadowColor)
                }
            }
        }
        .background(color = color, shape = shape)
        .border(width = borderWidth, color = borderColor, shape = shape)
}

/**
 * Applies a click handler with the ripple stripped out. Callers pair this with
 * [neoSurface] over a shared [interactionSource] so the press visual and the click
 * target stay in sync.
 */
fun Modifier.neoClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = interactionSource,
    indication = NeoNoIndication,
    enabled = enabled,
    role = role,
    onClickLabel = onClickLabel,
    onClick = onClick,
)

/** Remembers a [MutableInteractionSource] for components that are not given one. */
@Composable
fun rememberNeoInteractionSource(): MutableInteractionSource = remember { MutableInteractionSource() }

/**
 * Grows the reported size by the shadow offset while measuring the content into the
 * remaining space, so the shadow is painted inside the component's own bounds.
 */
private fun Modifier.reserveShadowGutter(offset: Dp): Modifier = layout { measurable, constraints ->
    val gutter = offset.roundToPx()
    val placeable = measurable.measure(constraints.offset(horizontal = -gutter, vertical = -gutter))
    layout(placeable.width + gutter, placeable.height + gutter) {
        placeable.place(0, 0)
    }
}

/** Moves the drawn body by [travel] without disturbing the reserved bounds. */
private fun Modifier.travelBy(travel: Dp): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    layout(placeable.width, placeable.height) {
        placeable.place(travel.roundToPx(), travel.roundToPx())
    }
}
