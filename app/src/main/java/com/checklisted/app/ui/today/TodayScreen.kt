package com.checklisted.app.ui.today

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.checklisted.app.ui.components.NeoButton
import com.checklisted.app.ui.components.NeoEmptyState
import com.checklisted.app.ui.components.NeoIconPlus
import com.checklisted.app.ui.components.NeoOutlineButton
import com.checklisted.app.ui.components.NeoProgressBar
import com.checklisted.app.ui.components.ReorderState
import com.checklisted.app.ui.components.rememberReorderState
import com.checklisted.app.ui.components.reorderable
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.condensed
import com.checklisted.app.ui.theme.displayUppercase

@Composable
fun TodayScreen(
    onCreateGoal: () -> Unit,
    onOpenGoal: (String) -> Unit,
    onOpenHistory: () -> Unit,
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
        onMove = viewModel::moveGoal,
        onCommitOrder = viewModel::commitOrder,
        onCancelReorder = viewModel::cancelReorder,
        modifier = modifier,
    )
}

@Composable
private fun TodayContent(
    state: TodayUiState,
    onToggle: (GoalStatus) -> Unit,
    onOpenGoal: (String) -> Unit,
    onCreateGoal: () -> Unit,
    onOpenHistory: () -> Unit,
    onMove: (String, String) -> Boolean,
    onCommitOrder: () -> Unit,
    onCancelReorder: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NeoTheme.colors
    val haptics = LocalHapticFeedback.current
    val listState = rememberLazyListState()
    val reorderState = rememberReorderState(
        listState = listState,
        onMove = onMove,
        onCommit = onCommitOrder,
        onCancel = onCancelReorder,
    )

    val insets = WindowInsets.systemBars
        .add(WindowInsets(left = 20.dp, top = 20.dp, right = 20.dp, bottom = 32.dp))
        .asPaddingValues()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = insets,
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = HEADER_KEY) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.today_title).displayUppercase(),
                        style = MaterialTheme.typography.displaySmall.condensed(),
                        color = colors.ink,
                    )
                    NeoOutlineButton(
                        text = stringResource(R.string.action_history),
                        onClick = onOpenHistory,
                    )
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
                        onDragStarted = { haptics.performHapticFeedback(HapticFeedbackType.LongPress) },
                    )
                }
            }
        }

        if (!state.hasNoGoals) {
            NeoButton(
                text = stringResource(R.string.action_new_goal),
                onClick = onCreateGoal,
                accent = NeoAccent.PINK,
                leadingIcon = { NeoIconPlus(tint = colors.onAccent, size = 18.dp) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(insets)
                    .padding(bottom = 4.dp, end = 4.dp),
            )
        }
    }
}

private fun LazyListScope.todaySection(
    section: TodaySection,
    reorderState: ReorderState,
    onToggle: (GoalStatus) -> Unit,
    onOpenGoal: (String) -> Unit,
    onDragStarted: () -> Unit,
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

    items(items = section.goals, key = { it.goal.id }) { status ->
        val dragging = reorderState.draggedKey == status.goal.id
        GoalRow(
            status = status,
            isDragging = dragging,
            onToggle = { onToggle(status) },
            onOpen = { onOpenGoal(status.goal.id) },
            modifier = Modifier
                .zIndex(if (dragging) 1f else 0f)
                .dragOffset(reorderState.offsetFor(status.goal.id))
                .reorderable(state = reorderState, key = status.goal.id, onDragStarted = onDragStarted)
                .padding(bottom = 10.dp),
        )
    }
}

@Composable
private fun SectionHeader(section: TodaySection) {
    val colors = NeoTheme.colors
    val accent = section.recurrence.accent()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = stringResource(section.recurrence.labelRes()).displayUppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.ink,
            )
            Text(
                text = stringResource(R.string.section_counter, section.completed, section.total),
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
            )
        }
        NeoProgressBar(progress = section.progress, accent = accent)
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

private fun Recurrence.accent() = when (this) {
    Recurrence.DAILY -> NeoAccent.YELLOW
    Recurrence.WEEKLY -> NeoAccent.TEAL
    Recurrence.MONTHLY -> NeoAccent.PURPLE
}

private const val HEADER_KEY = "today-header"
private const val EMPTY_KEY = "today-empty"
