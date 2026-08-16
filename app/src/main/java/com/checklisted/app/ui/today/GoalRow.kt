package com.checklisted.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.domain.model.GoalStatus
import com.checklisted.app.ui.components.NeoCard
import com.checklisted.app.ui.components.NeoCheckbox
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoTheme

private const val DRAG_SCALE = 1.03f

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
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
) {
    val colors = NeoTheme.colors
    val accent = NeoAccent.fromTag(status.goal.colorTag)

    // Dragging is unreachable with a screen reader, so the same reordering is
    // offered as custom actions. Without these the feature simply does not exist
    // for those users.
    val moveUpLabel = stringResource(R.string.a11y_move_up)
    val moveDownLabel = stringResource(R.string.a11y_move_down)
    val moveActions = buildList {
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
    }

    NeoCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { if (moveActions.isNotEmpty()) customActions = moveActions }
            .graphicsLayer {
                // Lifts the dragged row off the stack. Scale only — the shadow is a
                // hard offset and must not grow, or the row would look blurred.
                val scale = if (isDragging) DRAG_SCALE else 1f
                scaleX = scale
                scaleY = scale
            },
        color = colors.surface,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
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
