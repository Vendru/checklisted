package com.checklisted.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** One side of a [NeoSwipeRow]: what is revealed, and what happens when it is let go. */
@Immutable
class NeoSwipeAction(
    val label: String,
    val fill: Color,
    val contentColor: Color,
    val onTrigger: () -> Unit,
    /**
     * Drawn in the colour the drawer is currently using, which is not the action's own
     * until the swipe commits. Tinting it at the call site painted the icon white on
     * the neutral drawer, at 1.13:1 — a mark nobody could see for the first third of
     * every gesture.
     */
    val icon: @Composable (tint: Color) -> Unit,
)

/**
 * Horizontal travel of a swipeable row.
 *
 * Hoisted so a preview or a screenshot can render the row already open — the whole
 * point of these actions is what the drawer underneath looks like, and a component
 * that only ever renders at rest cannot be checked for it.
 */
@Stable
class NeoSwipeState internal constructor(initialOffsetPx: Float) {
    internal val offset = Animatable(initialOffsetPx)

    internal suspend fun settle() {
        offset.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = SETTLE_MS))
    }

    private companion object {
        const val SETTLE_MS = 160
    }
}

@Composable
fun rememberNeoSwipeState(initialReveal: Dp = 0.dp): NeoSwipeState {
    val px = with(LocalDensity.current) { initialReveal.toPx() }
    return remember { NeoSwipeState(px) }
}

/**
 * A row that reveals an action when dragged sideways.
 *
 * Written by hand rather than on Material's `SwipeToDismissBox` for two reasons: that
 * component settles into a dismissed state, which is wrong for an action like ticking
 * a goal off where the row has to stay, and its `confirmValueChange` is the only place
 * to hook the trigger while being called more than once per gesture.
 *
 * Nothing here is the only way to reach either action. A swipe is invisible to a
 * screen reader and awkward one-handed, so the checkbox still toggles and the row
 * still opens the editor; this is a shortcut, never the sole route.
 *
 * The drag is horizontal-only, which is what lets it share a row with the vertical
 * long-press reorder: a sideways move cancels the long press before it fires, and a
 * held finger never crosses the horizontal touch slop.
 */
@Composable
fun NeoSwipeRow(
    modifier: Modifier = Modifier,
    startAction: NeoSwipeAction? = null,
    endAction: NeoSwipeAction? = null,
    enabled: Boolean = true,
    state: NeoSwipeState = rememberNeoSwipeState(),
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var width by remember { mutableIntStateOf(0) }
    // Latched for the length of one gesture, so the haptic fires once on the way past
    // the commit point rather than on every frame beyond it.
    var buzzed by remember { mutableStateOf(false) }

    val offsetPx = state.offset.value
    val threshold = width * COMMIT_FRACTION
    val maxTravel = width * MAX_TRAVEL_FRACTION
    val committed = threshold > 0f && abs(offsetPx) >= threshold

    Box(
        modifier = modifier
            .onSizeChanged { width = it.width }
            // Only while open. The row's bounds are its own body, so clipping
            // unconditionally would also crop the 3% lift the reorder drag applies.
            .then(if (offsetPx != 0f) Modifier.clipToBounds() else Modifier),
    ) {
        val revealed = when {
            offsetPx > 0f -> startAction
            offsetPx < 0f -> endAction
            else -> null
        }
        if (revealed != null) {
            SwipeDrawer(
                action = revealed,
                alignment = if (offsetPx > 0f) Alignment.CenterStart else Alignment.CenterEnd,
                committed = committed,
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(offsetPx.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    enabled = enabled && (startAction != null || endAction != null),
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            val next = (state.offset.value + delta).coerceIn(
                                minimumValue = if (endAction != null) -maxTravel else 0f,
                                maximumValue = if (startAction != null) maxTravel else 0f,
                            )
                            state.offset.snapTo(next)

                            val past = threshold > 0f && abs(next) >= threshold
                            if (past && !buzzed) {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            buzzed = past
                        }
                    },
                    onDragStopped = {
                        val settled = state.offset.value
                        val action = when {
                            threshold <= 0f -> null
                            settled >= threshold -> startAction
                            settled <= -threshold -> endAction
                            else -> null
                        }
                        buzzed = false
                        // Fire before settling: the tick has to land the moment the
                        // finger lifts, not after the row has finished sliding back.
                        action?.onTrigger()
                        state.settle()
                    },
                ),
        ) {
            content()
        }
    }
}

/**
 * The block behind the row.
 *
 * Sized to the row's body rather than to its bounds: a row reserves its shadow inside
 * its own measured size, so a full-bleed drawer would stick out past the card on the
 * two sides the shadow occupies.
 */
@Composable
private fun BoxScope.SwipeDrawer(
    action: NeoSwipeAction,
    alignment: Alignment,
    committed: Boolean,
) {
    val colors = NeoTheme.colors

    // Colour arrives only once the drag has gone far enough that letting go will do
    // something. A drawer painted crimson from the first pixel would promise a delete
    // that a short swipe does not actually perform.
    val fill = if (committed) action.fill else colors.surfaceMuted
    val content = if (committed) action.contentColor else colors.inkSoft

    Box(
        modifier = Modifier
            .matchParentSize()
            .padding(end = NeoTokens.ShadowOffset, bottom = NeoTokens.ShadowOffset)
            .background(fill, NeoShapes.medium)
            .border(NeoTokens.BorderWidth, colors.divider, NeoShapes.medium),
        contentAlignment = alignment,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The icon sits against the outer edge and the word inboard of it, so the
            // first thing a short swipe uncovers is the icon. The word waits for the
            // commit point, which is set wide enough to fit it: revealed any earlier it
            // came out sliced down the middle, and "ir" is not a label.
            if (alignment == Alignment.CenterStart) {
                action.icon(content)
                if (committed) DrawerLabel(label = action.label, color = content)
            } else {
                if (committed) DrawerLabel(label = action.label, color = content)
                action.icon(content)
            }
        }
    }
}

@Composable
private fun DrawerLabel(label: String, color: Color) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = color,
        maxLines = 1,
    )
}

/**
 * How far across the row the finger has to travel before letting go does anything.
 *
 * Also the width the drawer's label gets: at a third of a 371.dp row the longest of
 * them ("Concluir", plus its icon and padding) needs about 126.dp, and this leaves
 * room to spare.
 */
private const val COMMIT_FRACTION = 0.36f

/** The row never leaves the screen: this is a drawer, not a dismissal. */
private const val MAX_TRAVEL_FRACTION = 0.52f

@NeoPreviews
@Composable
private fun NeoSwipeRowPreview() {
    PreviewStack {
        NeoSwipeRowSamples()
    }
}

@Composable
internal fun NeoSwipeRowSamples() {
    val colors = NeoTheme.colors

    NeoSwipeRow(
        modifier = Modifier.fillMaxWidth(),
        state = rememberNeoSwipeState(initialReveal = 150.dp),
        startAction = NeoSwipeAction(
            label = stringResource(R.string.action_complete),
            fill = colors.action,
            contentColor = colors.onAction,
            onTrigger = {},
            icon = { tint -> NeoIconCheck(tint = tint, size = 18.dp) },
        ),
    ) {
        NeoCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.sample_goal_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }

    NeoSwipeRow(
        modifier = Modifier.fillMaxWidth(),
        state = rememberNeoSwipeState(initialReveal = (-150).dp),
        endAction = NeoSwipeAction(
            label = stringResource(R.string.action_delete),
            fill = colors.danger,
            contentColor = colors.onDanger,
            onTrigger = {},
            icon = { tint -> NeoIconTrash(tint = tint, size = 18.dp) },
        ),
    ) {
        NeoCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.sample_goal_title),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}
