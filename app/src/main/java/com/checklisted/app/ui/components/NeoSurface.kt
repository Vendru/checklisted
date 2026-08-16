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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
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
 * both the border and the shadow. This is installed as `LocalIndication` by
 * `NeoTheme`, so an ordinary `Modifier.clickable` anywhere in the tree is
 * ripple-free without having to opt out.
 */
object NeoNoIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode = Node()

    private class Node : Modifier.Node(), DrawModifierNode {
        override fun ContentDrawScope.draw() = drawContent()
    }

    // IndicationNodeFactory declares both as abstract so implementations cannot
    // accidentally rely on identity; for an object, identity is the right answer.
    override fun hashCode(): Int = "NeoNoIndication".hashCode()

    override fun equals(other: Any?): Boolean = other === this
}

/**
 * Paints the neobrutalist body of a component: a solid offset shadow with no blur,
 * a flat fill, and a heavy border.
 *
 * While [pressed], the body travels [shadowOffset] in both axes and the shadow is
 * consumed by exactly as much, so the component reads as sinking onto its own
 * shadow. The travel runs on a 40 ms linear tween — this style has no room for soft
 * easing.
 *
 * The travel is applied **in the draw phase only**. Moving the component's
 * placement instead would drag every descendant's layout bounds with it, including
 * the pointer-input node of whatever click modifier is chained after this one: a
 * finger that landed within [shadowOffset] of the leading edge would fall outside
 * the node's recomputed local bounds and the gesture would be cancelled mid-press.
 * Draw modifiers do not participate in hit testing, so translating here keeps the
 * touch target exactly where the user aimed.
 *
 * The shadow gutter is reserved inside the component's own measured bounds, so
 * pressing one component never nudges its neighbours.
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
    disabledColor: Color = NeoTheme.colors.surfaceDisabled,
): Modifier {
    val travel by animateDpAsState(
        targetValue = if (pressed && enabled) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = 40, easing = LinearEasing),
        label = "neoPressTravel",
    )
    // Disabled swaps the fill and keeps the border and shadow at full strength.
    // Fading the whole component would wash the ink to grey, which reads as the
    // low-contrast state this design has no room for.
    val fill = if (enabled) color else disabledColor

    return this
        .reserveShadowGutter(shadowOffset)
        .drawWithCache {
            // Cached per size/shape rather than rebuilt on every draw pass — the
            // press tween invalidates draw on each frame.
            val outline = shape.createOutline(size, layoutDirection, this)
            onDrawWithContent {
                val travelPx = travel.toPx()
                val remaining = shadowOffset.toPx() - travelPx
                if (remaining > 0f) {
                    translate(left = remaining, top = remaining) {
                        drawOutline(outline, shadowColor)
                    }
                }
                translate(left = travelPx, top = travelPx) {
                    this@onDrawWithContent.drawContent()
                }
            }
        }
        .background(color = fill, shape = shape)
        .border(width = borderWidth, color = borderColor, shape = shape)
}

/**
 * Applies a click handler wired to the shared [interactionSource] so the press
 * visual produced by [neoSurface] stays in sync with the gesture.
 *
 * `indication` is null rather than [NeoNoIndication]: the press treatment is
 * already drawn by [neoSurface], so there is nothing left for an indication to do.
 */
fun Modifier.neoClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    role: Role? = Role.Button,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    role = role,
    onClickLabel = onClickLabel,
    onClick = onClick,
)

/**
 * Checkbox-style toggle.
 *
 * Uses [toggleable] rather than [clickable] with `Role.Checkbox`: only `toggleable`
 * writes `ToggleableState` into semantics, which is what a screen reader announces
 * as checked/unchecked. A role alone carries no state.
 */
fun Modifier.neoToggleable(
    value: Boolean,
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    role: Role? = Role.Checkbox,
    onValueChange: (Boolean) -> Unit,
): Modifier = toggleable(
    value = value,
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    role = role,
    onValueChange = onValueChange,
)

/**
 * Single-choice option.
 *
 * Uses [selectable] rather than [clickable] with `Role.RadioButton`, for the same
 * reason as [neoToggleable]: only `selectable` sets `SemanticsProperties.Selected`.
 */
fun Modifier.neoSelectable(
    selected: Boolean,
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    role: Role? = Role.RadioButton,
    onClick: () -> Unit,
): Modifier = selectable(
    selected = selected,
    interactionSource = interactionSource,
    indication = null,
    enabled = enabled,
    role = role,
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
