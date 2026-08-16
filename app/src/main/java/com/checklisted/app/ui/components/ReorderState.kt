package com.checklisted.app.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Drag-to-reorder for a [androidx.compose.foundation.lazy.LazyColumn] keyed by
 * stable ids.
 *
 * The dragged row is offset visually while the underlying list is reordered as soon
 * as the row's centre crosses a neighbour, so what the user sees mid-drag is the
 * order they will get. [onMove] returns false to refuse a move — the Today screen
 * uses that to keep goals inside their own recurrence section.
 */
@Stable
class ReorderState internal constructor(
    private val listState: LazyListState,
    private val onMove: (draggedKey: String, targetKey: String) -> Boolean,
    private val onCommit: () -> Unit,
    private val onCancel: () -> Unit,
) {
    var draggedKey: String? by mutableStateOf(null)
        private set

    private var draggedDistance by mutableFloatStateOf(0f)
    private var draggedOrigin = 0
    private var draggedSize = 0

    /** Vertical offset to apply to the row currently being dragged. */
    fun offsetFor(key: String): Float = if (key == draggedKey) draggedDistance else 0f

    fun onDragStart(key: String) {
        val item = visibleItem(key) ?: return
        draggedKey = key
        draggedOrigin = item.offset
        draggedSize = item.size
        draggedDistance = 0f
    }

    fun onDrag(delta: Float) {
        if (draggedKey == null) return
        draggedDistance += delta
        checkForSwap()
    }

    /**
     * Moves the row into a neighbour's slot once its centre has crossed into one.
     *
     * Also called while auto-scrolling: the finger can be perfectly still and still
     * need to swap, because the list is moving underneath it.
     */
    fun checkForSwap() {
        val key = draggedKey ?: return
        val top = draggedOrigin + draggedDistance
        val centre = (top + draggedSize / 2f).toInt()

        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
            info.key != key && centre in info.offset..(info.offset + info.size)
        } ?: return
        val targetKey = target.key as? String ?: return

        if (!onMove(key, targetKey)) return

        // The row has taken the target's slot. Rebase so the finger keeps its grip on
        // the same point of the row instead of the row jumping out from under it.
        draggedOrigin = target.offset
        draggedDistance = top - target.offset
    }

    /**
     * How far the list should scroll this frame, in pixels.
     *
     * Non-zero once the dragged row reaches within one row-height of either edge of
     * the viewport, and proportional to how far past that threshold it is, so nudging
     * the edge crawls and pinning the row to it moves fast.
     */
    fun autoScrollDelta(): Float {
        if (draggedKey == null) return 0f
        val info = listState.layoutInfo
        val top = draggedOrigin + draggedDistance
        val bottom = top + draggedSize
        val threshold = draggedSize.toFloat()

        val lowerBound = info.viewportEndOffset - threshold
        val upperBound = info.viewportStartOffset + threshold

        return when {
            bottom > lowerBound -> (bottom - lowerBound).coerceAtMost(MAX_SCROLL_PER_FRAME)
            top < upperBound -> (top - upperBound).coerceAtLeast(-MAX_SCROLL_PER_FRAME)
            else -> 0f
        }
    }

    /**
     * Compensates for a scroll the drag itself caused.
     *
     * Scrolling moves every slot, including the dragged row's. Shifting the origin
     * and the distance by the same amount in opposite directions leaves the row
     * painted exactly where the finger is holding it.
     */
    fun onAutoScrolled(consumed: Float) {
        if (draggedKey == null || consumed == 0f) return
        draggedOrigin -= consumed.toInt()
        draggedDistance += consumed
    }

    fun onDragEnd() {
        if (draggedKey != null) onCommit()
        reset()
    }

    fun onDragCancel() {
        if (draggedKey != null) onCancel()
        reset()
    }

    private fun reset() {
        draggedKey = null
        draggedDistance = 0f
        draggedOrigin = 0
        draggedSize = 0
    }

    private fun visibleItem(key: String) =
        listState.layoutInfo.visibleItemsInfo.firstOrNull { it.key == key }

    private companion object {
        /** Caps the speed so a fast drag cannot fling the list past its target. */
        const val MAX_SCROLL_PER_FRAME = 18f
    }
}

@Composable
fun rememberReorderState(
    listState: LazyListState,
    onMove: (draggedKey: String, targetKey: String) -> Boolean,
    onCommit: () -> Unit,
    onCancel: () -> Unit,
): ReorderState {
    val currentMove by rememberUpdatedState(onMove)
    val currentCommit by rememberUpdatedState(onCommit)
    val currentCancel by rememberUpdatedState(onCancel)
    return remember(listState) {
        ReorderState(
            listState = listState,
            onMove = { dragged, target -> currentMove(dragged, target) },
            onCommit = { currentCommit() },
            onCancel = { currentCancel() },
        )
    }
}

/**
 * Makes a row draggable after a long press.
 *
 * [onDragStarted] fires the haptic — the press has to be confirmed by touch, since
 * nothing else tells the user the row is now theirs to move.
 */
fun Modifier.reorderable(
    state: ReorderState,
    key: String,
    onDragStarted: () -> Unit = {},
): Modifier = pointerInput(key) {
    detectDragGesturesAfterLongPress(
        onDragStart = {
            state.onDragStart(key)
            onDragStarted()
        },
        onDrag = { change, amount ->
            change.consume()
            state.onDrag(amount.y)
        },
        onDragEnd = { state.onDragEnd() },
        onDragCancel = { state.onDragCancel() },
    )
}
