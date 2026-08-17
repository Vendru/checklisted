package com.checklisted.app.ui.archive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checklisted.app.R
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.ui.components.NeoBackButton
import com.checklisted.app.ui.components.NeoButton
import com.checklisted.app.ui.components.NeoCard
import com.checklisted.app.ui.components.NeoDialog
import com.checklisted.app.ui.components.NeoEmptyState
import com.checklisted.app.ui.components.NeoOutlineButton
import com.checklisted.app.ui.components.readableWidth
import com.checklisted.app.ui.theme.NeoTheme

@Composable
fun ArchivedGoalsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchivedGoalsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ArchivedGoalsContent(
        state = state,
        onBack = onBack,
        onUnarchive = viewModel::unarchive,
        onDelete = viewModel::delete,
        modifier = modifier,
    )
}

/** The screen without its view model, so a screenshot can render it. */
@Composable
internal fun ArchivedGoalsContent(
    state: ArchivedGoalsUiState,
    onBack: () -> Unit,
    onUnarchive: (String) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NeoTheme.colors
    // Saved, and carrying the title rather than looking it up: the dialog must not
    // need the list to have loaded before it can draw itself.
    var pendingDeleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingDeleteTitle by rememberSaveable { mutableStateOf("") }

    val insets = WindowInsets.systemBars
        .add(WindowInsets(left = 20.dp, top = 20.dp, right = 20.dp, bottom = 32.dp))
        .asPaddingValues()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .readableWidth()
            .verticalScroll(rememberScrollState())
            .padding(insets),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoBackButton(onClick = onBack)
            Text(
                text = stringResource(R.string.archived_title),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
        }

        if (!state.isLoading && state.goals.isEmpty()) {
            NeoEmptyState(
                title = stringResource(R.string.empty_archived_title),
                message = stringResource(R.string.empty_archived_message),
            )
        } else {
            Text(
                text = stringResource(R.string.archived_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.inkSoft,
                modifier = Modifier.padding(bottom = 6.dp),
            )
            state.goals.forEach { goal ->
                ArchivedGoalCard(
                    goal = goal,
                    onUnarchive = { onUnarchive(goal.id) },
                    onDelete = {
                        pendingDeleteId = goal.id
                        pendingDeleteTitle = goal.title
                    },
                )
            }
        }
    }

    val doomed = pendingDeleteId
    if (doomed != null) {
        NeoDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_goal_message, pendingDeleteTitle),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            destructive = true,
            onConfirm = {
                pendingDeleteId = null
                onDelete(doomed)
            },
            onDismissRequest = { pendingDeleteId = null },
        )
    }
}

@Composable
private fun ArchivedGoalCard(
    goal: Goal,
    onUnarchive: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = NeoTheme.colors

    NeoCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
            )
            Text(
                text = stringResource(goal.recurrence.labelRes()),
                style = MaterialTheme.typography.bodySmall,
                color = colors.inkSoft,
            )
        }

        // The two actions stack on their own line rather than sharing one with the
        // title: a long goal name plus two buttons is exactly the crowded row that
        // broke on a real phone before.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Deleting is confirmed in crimson by the dialog; the card keeps one
            // filled block, and it belongs to the reason the user came here.
            NeoOutlineButton(text = stringResource(R.string.action_delete), onClick = onDelete)
            NeoButton(text = stringResource(R.string.action_unarchive), onClick = onUnarchive)
        }
    }
}

private fun Recurrence.labelRes() = when (this) {
    Recurrence.DAILY -> R.string.recurrence_daily
    Recurrence.WEEKLY -> R.string.recurrence_weekly
    Recurrence.MONTHLY -> R.string.recurrence_monthly
}
