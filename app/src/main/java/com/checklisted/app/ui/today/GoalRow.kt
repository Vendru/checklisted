package com.checklisted.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.domain.model.GoalStatus
import com.checklisted.app.ui.components.NeoCard
import com.checklisted.app.ui.components.NeoCheckbox
import com.checklisted.app.ui.components.NeoIconCheck
import com.checklisted.app.ui.components.NeoIconTrash
import com.checklisted.app.ui.components.NeoSwipeAction
import com.checklisted.app.ui.components.NeoSwipeRow
import com.checklisted.app.ui.components.NeoSwipeState
import com.checklisted.app.ui.components.neoSurface
import com.checklisted.app.ui.components.rememberNeoSwipeState
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoCombCell
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

private const val DRAG_SCALE = 1.03f

/** One completed period is not a streak; two is the first one worth showing. */
private const val MIN_VISIBLE_STREAK = 2

/**
 * One goal on the Today screen.
 *
 * The whole row opens the editor; the checkbox owns its own 48.dp target inside it,
 * so ticking a goal never opens the editor by accident.
 */
@Composable
fun GoalRow(
    status: GoalStatus,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    streak: Int = 0,
    onDelete: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    swipeState: NeoSwipeState = rememberNeoSwipeState(),
) {
    val colors = NeoTheme.colors
    val accent = NeoAccent.fromTag(status.goal.colorTag)

    // Neither dragging nor swiping exists for a screen reader, so everything they
    // reach is also offered as a custom action. Without these the features simply do
    // not exist for those users.
    val moveUpLabel = stringResource(R.string.a11y_move_up)
    val moveDownLabel = stringResource(R.string.a11y_move_down)
    val deleteLabel = stringResource(R.string.a11y_delete_goal)
    val rowActions = buildList {
        onMoveUp?.let {
            add(
                CustomAccessibilityAction(moveUpLabel) {
                    it()
                    true
                },
            )
        }
        onMoveDown?.let {
            add(
                CustomAccessibilityAction(moveDownLabel) {
                    it()
                    true
                },
            )
        }
        onDelete?.let {
            add(
                CustomAccessibilityAction(deleteLabel) {
                    it()
                    true
                },
            )
        }
    }

    NeoSwipeRow(
        modifier = modifier.fillMaxWidth(),
        state = swipeState,
        // A row being dragged up the list must not also slide sideways.
        enabled = !isDragging,
        startAction = NeoSwipeAction(
            label = stringResource(
                if (status.isCompleted) R.string.action_reopen else R.string.action_complete,
            ),
            fill = colors.action,
            contentColor = colors.onAction,
            onTrigger = onToggle,
            icon = { tint -> NeoIconCheck(tint = tint, size = 18.dp) },
        ),
        endAction = onDelete?.let { delete ->
            NeoSwipeAction(
                label = stringResource(R.string.action_delete),
                fill = colors.danger,
                contentColor = colors.onDanger,
                onTrigger = delete,
                icon = { tint -> NeoIconTrash(tint = tint, size = 18.dp) },
            )
        },
    ) {
        GoalCard(
            status = status,
            accent = accent,
            streak = streak,
            isDragging = isDragging,
            rowActions = rowActions,
            onToggle = onToggle,
            onOpen = onOpen,
        )
    }
}

@Composable
private fun GoalCard(
    status: GoalStatus,
    accent: NeoAccent,
    streak: Int,
    isDragging: Boolean,
    rowActions: List<CustomAccessibilityAction>,
    onToggle: () -> Unit,
    onOpen: () -> Unit,
) {
    val colors = NeoTheme.colors

    NeoCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { if (rowActions.isNotEmpty()) customActions = rowActions }
            .graphicsLayer {
                // Lifts the dragged row off the stack. Scale only — the shadow is a
                // hard offset and must not grow, or the row would look blurred.
                val scale = if (isDragging) DRAG_SCALE else 1f
                scaleX = scale
                scaleY = scale
            },
        color = colors.surface,
        contentPadding = PaddingValues(14.dp),
        onClick = onOpen,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Ticking is an action, so the box wears the action colour. Which goal
            // this is gets told by the dot on the far side.
            NeoCheckbox(
                checked = status.isCompleted,
                onCheckedChange = { onToggle() },
                contentDescription = status.goal.title,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = status.goal.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (status.isCompleted) colors.inkSoft else colors.ink,
                    textDecoration = if (status.isCompleted) TextDecoration.LineThrough else null,
                )
                val description = status.goal.description
                if (!description.isNullOrBlank()) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.inkSoft,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            // Only from two up. A "1" next to every goal ticked once would be
            // wallpaper; appearing on the second period is what makes it read as
            // something earned.
            if (streak >= MIN_VISIBLE_STREAK) {
                StreakBadge(streak = streak)
            }

            // The tag: 8.dp of the goal's own colour. Small enough to stay quiet,
            // saturated enough to identify the row at a glance.
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accent.color, CircleShape),
            )
        }
    }
}

/**
 * How many periods in a row this goal has been kept.
 *
 * Wears the comb cell rather than a pill, and the muted wax rather than the action
 * colour: the row already has one honey block in the checkbox, and a second would
 * compete with the only thing on the row that is actually tappable.
 */
@Composable
private fun StreakBadge(streak: Int) {
    val colors = NeoTheme.colors
    val spoken = pluralStringResource(R.plurals.a11y_streak, streak, streak)

    Box(
        modifier = Modifier
            .neoSurface(
                color = colors.surfaceMuted,
                shape = NeoCombCell,
                shadowOffset = 0.dp,
                borderWidth = NeoTokens.HairlineBorder,
            )
            // Grows with the number instead of clipping it: a daily goal kept for
            // five months is a three-digit streak, and that is exactly the user this
            // badge is for.
            .defaultMinSize(minWidth = 26.dp, minHeight = 26.dp)
            .padding(horizontal = 6.dp)
            .semantics { contentDescription = spoken },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.streak_badge, streak),
            style = MaterialTheme.typography.labelSmall,
            color = colors.ink,
        )
    }
}
