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
import com.checklisted.app.ui.components.NeoBackButton
import com.checklisted.app.ui.components.NeoCheckbox
import com.checklisted.app.ui.components.NeoDialog
import com.checklisted.app.ui.components.NeoEmptyState
import com.checklisted.app.ui.components.NeoHeatmap
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.condensed
import com.checklisted.app.ui.theme.displayUppercase
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = NeoTheme.colors

    val insets = WindowInsets.systemBars
        .add(WindowInsets(left = 20.dp, top = 20.dp, right = 20.dp, bottom = 32.dp))
        .asPaddingValues()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
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
                text = stringResource(R.string.history_title).displayUppercase(),
                style = MaterialTheme.typography.headlineMedium.condensed(),
                color = colors.ink,
            )
        }

        if (!state.isLoading && state.goalCount == 0) {
            NeoEmptyState(
                title = stringResource(R.string.empty_history_title),
                message = stringResource(R.string.empty_history_message),
                accent = NeoAccent.PURPLE,
            )
        } else {
            Text(
                text = stringResource(R.string.history_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.ink,
            )
            NeoHeatmap(
                days = state.heatmap,
                accent = NeoAccent.PURPLE,
                onDayClick = { day -> viewModel.selectDay(day.date) },
            )
        }
    }

    val selected = state.selectedDay
    if (selected != null) {
        NeoDialog(
            title = selected.date.format(
                DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.getDefault()),
            ),
            message = stringResource(R.string.history_day_hint),
            dismissText = stringResource(R.string.action_close),
            onDismissRequest = viewModel::dismissDay,
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                selected.goals.forEach { dayGoal ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        NeoCheckbox(
                            checked = dayGoal.isCompleted,
                            onCheckedChange = { viewModel.toggle(dayGoal) },
                            accent = NeoAccent.fromTag(dayGoal.goal.colorTag),
                            contentDescription = dayGoal.goal.title,
                        )
                        Text(
                            text = dayGoal.goal.title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = NeoTheme.colors.ink,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}
