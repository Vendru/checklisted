package com.checklisted.app.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checklisted.app.R
import com.checklisted.app.domain.model.GoalStatus
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.ui.AppLocale
import com.checklisted.app.ui.components.NeoButton
import com.checklisted.app.ui.components.NeoDialog
import com.checklisted.app.ui.components.NeoEmptyState
import com.checklisted.app.ui.components.NeoIconButton
import com.checklisted.app.ui.components.NeoIconPlus
import com.checklisted.app.ui.components.NeoIconSettings
import com.checklisted.app.ui.components.NeoOutlineButton
import com.checklisted.app.ui.components.NeoProgressBar
import com.checklisted.app.ui.components.ReorderState
import com.checklisted.app.ui.components.rememberReorderState
import com.checklisted.app.ui.components.reorderable
import com.checklisted.app.ui.theme.NeoTheme
import kotlinx.coroutines.isActive
import java.time.format.DateTimeFormatter

@Composable
fun TodayScreen(
    onCreateGoal: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodayViewModel = hiltViewModel(),
) {
    // Lifecycle-aware so the period clock is re-read whenever the screen comes back
    // to the foreground, which is the revalidation the rollover depends on.
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TodayContent(
        state = state,
        onToggle = viewModel::toggle,
        onOpenGoal = onOpenGoal,
        onCreateGoal = onCreateGoal,
        onOpenHistory = onOpenHistory,
        onOpenSettings = onOpenSettings,
        onDelete = viewModel::delete,
        onMove = viewModel::moveGoal,
        onCommitOrder = viewModel::commitOrder,
        onCancelReorder = viewModel::cancelReorder,
        modifier = modifier,
    )
}

/**
 * The screen without its view model, so a screenshot can render it with a realistic
 * list. Component-level images missed a full-height grid and an overflowing chip row;
 * whole screens with real data are what catch that class of defect.
 */
@Composable
internal fun TodayContent(
    state: TodayUiState,
    onToggle: (GoalStatus) -> Unit,
    onOpenGoal: (String) -> Unit,
    onCreateGoal: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onDelete: (String) -> Unit,
    onMove: (String, String) -> Boolean,
    onCommitOrder: () -> Unit,
    onCancelReorder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NeoTheme.colors
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    // Deleting takes the goal's history with it, so the swipe only ever opens this
    // question. Held here rather than in the row: the row is inside a LazyColumn and
    // is disposed the moment the list scrolls, which would close the dialog.
    //
    // Saved rather than remembered, and by id rather than by value: a rotation used
    // to dismiss the question mid-decision, and the goal it refers to is not itself
    // something that survives a process death — it is re-read from state below.
    var pendingDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    val reorderState = rememberReorderState(
        listState = listState,
        onMove = onMove,
        onCommit = onCommitOrder,
        onCancel = onCancelReorder,
    )

    // Drives the list while a row is held against either edge of the viewport.
    LaunchedEffect(reorderState.draggedKey) {
        if (reorderState.draggedKey == null) return@LaunchedEffect
        while (isActive) {
            val delta = reorderState.autoScrollDelta()
            if (delta != 0f) {
                val consumed = listState.scrollBy(delta)
                reorderState.onAutoScrolled(consumed)
                reorderState.checkForSwap()
            }
            withFrameNanos { }
        }
    }

    val screenInsets = WindowInsets.systemBars
        .add(WindowInsets(left = 20.dp, top = 20.dp, right = 20.dp, bottom = 32.dp))
    val insets = screenInsets.asPaddingValues()

    // The new-goal button floats over the list, so the list has to be able to scroll
    // past it. Without this the last goal of the last section sits permanently under
    // the button and can never be reached.
    val listInsets = screenInsets
        .add(WindowInsets(bottom = if (state.hasNoGoals) 0.dp else FabGutter))
        .asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        // Spacing is per item, not per section: a list arrangement of 20.dp applied
        // between every row too, so consecutive goals sat 30.dp apart and three goals
        // filled a screen. Sections get their air from the header's own top padding.
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = listInsets,
            verticalArrangement = Arrangement.spacedBy(RowGap),
        ) {
            item(key = HEADER_KEY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.today_title),
                            style = MaterialTheme.typography.displaySmall,
                            color = colors.ink,
                        )
                        // The date was computed, carried through the view model and
                        // thrown away. In an app whose whole logic is "which period is
                        // this", saying so out loud is worth a line — especially near
                        // midnight, when the list silently re-keys itself.
                        val date = state.date
                        if (date != null) {
                            Text(
                                text = remember(date) {
                                    date.format(
                                        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", AppLocale),
                                    )
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.inkSoft,
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        NeoOutlineButton(
                            text = stringResource(R.string.action_history),
                            onClick = onOpenHistory,
                        )
                        NeoIconButton(
                            onClick = onOpenSettings,
                            contentDescription = stringResource(R.string.action_settings),
                        ) {
                            NeoIconSettings()
                        }
                    }
                }
            }

            if (state.hasNoGoals) {
                item(key = EMPTY_KEY) {
                    NeoEmptyState(
                        title = stringResource(R.string.empty_today_title),
                        message = stringResource(R.string.empty_today_message),
                        action = {
                            NeoButton(text = stringResource(R.string.action_new_goal), onClick = onCreateGoal)
                        },
                    )
                }
            } else {
                state.sections.forEach { section ->
                    todaySection(
                        section = section,
                        reorderState = reorderState,
                        onToggle = onToggle,
                        onOpenGoal = onOpenGoal,
                        onRequestDelete = { status -> pendingDeleteId = status.goal.id },
                        onDragStarted = { haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
                        onReorder = { dragged, target ->
                            if (onMove(dragged, target)) onCommitOrder()
                        },
                    )
                }
            }
        }

        if (!state.hasNoGoals) {
            NeoButton(
                text = stringResource(R.string.action_new_goal),
                onClick = onCreateGoal,
                leadingIcon = { NeoIconPlus(tint = colors.onAction, size = 18.dp) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(insets)
                    .padding(bottom = 4.dp, end = 4.dp),
            )
        }
    }

    // Resolves to null if the goal disappeared while the dialog was up, which closes
    // the dialog rather than leaving it asking about something that no longer exists.
    val doomed = pendingDeleteId?.let { id ->
        state.sections.firstNotNullOfOrNull { section ->
            section.goals.firstOrNull { it.goal.id == id }
        }
    }
    if (doomed != null) {
        NeoDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_goal_message, doomed.goal.title),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = {
                pendingDeleteId = null
                onDelete(doomed.goal.id)
            },
            onDismissRequest = { pendingDeleteId = null },
        )
    }
}

private fun LazyListScope.todaySection(
    section: TodaySection,
    reorderState: ReorderState,
    onToggle: (GoalStatus) -> Unit,
    onOpenGoal: (String) -> Unit,
    onRequestDelete: (GoalStatus) -> Unit,
    onDragStarted: () -> Unit,
    onReorder: (String, String) -> Unit,
) {
    item(key = "header-${section.recurrence.name}") {
        SectionHeader(section = section)
    }

    if (section.goals.isEmpty()) {
        item(key = "empty-${section.recurrence.name}") {
            Text(
                text = stringResource(section.recurrence.emptyMessageRes()),
                style = MaterialTheme.typography.bodyMedium,
                color = NeoTheme.colors.ink,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
    }

    itemsIndexed(items = section.goals, key = { _, status -> status.goal.id }) { index, status ->
        val dragging = reorderState.draggedKey == status.goal.id
        val previousId = section.goals.getOrNull(index - 1)?.goal?.id
        val nextId = section.goals.getOrNull(index + 1)?.goal?.id

        GoalRow(
            status = status,
            isDragging = dragging,
            onToggle = { onToggle(status) },
            onOpen = { onOpenGoal(status.goal.id) },
            onDelete = { onRequestDelete(status) },
            onMoveUp = previousId?.let { target -> { onReorder(status.goal.id, target) } },
            onMoveDown = nextId?.let { target -> { onReorder(status.goal.id, target) } },
            modifier = Modifier
                .zIndex(if (dragging) 1f else 0f)
                .dragOffset(reorderState.offsetFor(status.goal.id))
                .reorderable(state = reorderState, key = status.goal.id, onDragStarted = onDragStarted),
        )
    }
}

@Composable
private fun SectionHeader(section: TodaySection) {
    val colors = NeoTheme.colors

    Column(
        modifier = Modifier.padding(top = SectionGap),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(section.recurrence.labelRes()),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.ink,
            )
            Text(
                text = stringResource(R.string.section_counter, section.completed, section.total),
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
            )
        }
        NeoProgressBar(progress = section.progress)
    }
}

/**
 * Shifts the dragged row by the finger's travel.
 *
 * Applied at placement rather than as a layout offset so the rows around it keep
 * their own positions and the list does not reflow under the drag.
 */
private fun Modifier.dragOffset(offsetY: Float): Modifier =
    if (offsetY == 0f) {
        this
    } else {
        layout { measurable, constraints ->
            val placeable = measurable.measure(constraints)
            layout(placeable.width, placeable.height) {
                placeable.placeRelative(0, offsetY.toInt())
            }
        }
    }

private fun Recurrence.labelRes() = when (this) {
    Recurrence.DAILY -> R.string.recurrence_daily
    Recurrence.WEEKLY -> R.string.recurrence_weekly
    Recurrence.MONTHLY -> R.string.recurrence_monthly
}

private fun Recurrence.emptyMessageRes() = when (this) {
    Recurrence.DAILY -> R.string.empty_section_daily
    Recurrence.WEEKLY -> R.string.empty_section_weekly
    Recurrence.MONTHLY -> R.string.empty_section_monthly
}

/** Between two goals in the same section. */
private val RowGap = 10.dp

/** Added on top of [RowGap] before a section heading. */
private val SectionGap = 14.dp

/** Button height plus its offset from the corner, rounded up. */
private val FabGutter = 60.dp

private const val HEADER_KEY = "today-header"
private const val EMPTY_KEY = "today-empty"
