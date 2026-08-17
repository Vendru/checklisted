package com.checklisted.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checklisted.app.R
import com.checklisted.app.ui.AppLocale
import com.checklisted.app.ui.components.NeoBackButton
import com.checklisted.app.ui.components.NeoCheckbox
import com.checklisted.app.ui.components.NeoDialog
import com.checklisted.app.ui.components.NeoEmptyState
import com.checklisted.app.ui.components.NeoHeatmap
import com.checklisted.app.ui.components.readableWidth
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HistoryContent(
        state = state,
        onBack = onBack,
        onDayClick = viewModel::selectDay,
        onToggle = viewModel::toggle,
        onDismissDay = viewModel::dismissDay,
        modifier = modifier,
    )
}

/** The screen without its view model, so a screenshot can render it. */
@Composable
internal fun HistoryContent(
    state: HistoryUiState,
    onBack: () -> Unit,
    onDayClick: (java.time.LocalDate) -> Unit,
    onToggle: (DayGoal) -> Unit,
    onDismissDay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NeoTheme.colors

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
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoBackButton(onClick = onBack)
            Text(
                text = stringResource(R.string.history_title),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
            )
        }

        if (!state.isLoading && state.goalCount == 0) {
            NeoEmptyState(
                title = stringResource(R.string.empty_history_title),
                message = stringResource(R.string.empty_history_message),
            )
        } else {
            Text(
                text = stringResource(R.string.history_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink,
            )
            NeoHeatmap(
                days = state.heatmap,
                onDayClick = { day -> onDayClick(day.date) },
            )
        }
    }

    val selected = state.selectedDay
    if (selected != null) {
        NeoDialog(
            title = selected.date.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(AppLocale),
            ),
            message = stringResource(R.string.history_day_hint),
            dismissText = stringResource(R.string.action_close),
            onDismissRequest = onDismissDay,
        ) {
            DayGoalList(goals = selected.goals, onToggle = onToggle)
        }
    }
}

/**
 * The checkable goals inside the day sheet.
 *
 * Its own composable so a screenshot can put it in a dialog body directly: Paparazzi
 * pins a real dialog window to a fixed width and clips anything wider, which hides
 * exactly the wrapping this list needs to be checked for.
 */
@Composable
internal fun DayGoalList(
    goals: List<DayGoal>,
    onToggle: (DayGoal) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .heightIn(max = 320.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        goals.forEach { dayGoal ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NeoCheckbox(
                    checked = dayGoal.isCompleted,
                    onCheckedChange = { onToggle(dayGoal) },
                    accent = NeoAccent.fromTag(dayGoal.goal.colorTag),
                    contentDescription = dayGoal.goal.title,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dayGoal.goal.title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = NeoTheme.colors.ink,
                    )
                    // The sheet lists archived goals too, because their history
                    // happened and the day would otherwise have holes. Unlabelled they
                    // read as goals the user simply forgot about today.
                    if (dayGoal.goal.isArchived) {
                        Text(
                            text = stringResource(R.string.history_day_archived),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeoTheme.colors.inkSoft,
                        )
                    }
                }
            }
        }
    }
}
