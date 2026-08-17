package com.checklisted.app.ui.goal

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checklisted.app.R
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.ui.components.NeoBackButton
import com.checklisted.app.ui.components.NeoHeatmap
import com.checklisted.app.ui.components.NeoOutlineButton
import com.checklisted.app.ui.components.NeoStatRow
import com.checklisted.app.ui.components.readableWidth
import com.checklisted.app.ui.theme.NeoTheme
import java.time.LocalDate

@Composable
fun GoalDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // The goal can vanish under us if it is deleted from the editor.
    LaunchedEffect(state.isMissing) {
        if (state.isMissing) onBack()
    }

    GoalDetailContent(
        state = state,
        onBack = onBack,
        onEdit = onEdit,
        onDayClick = viewModel::toggleDay,
        modifier = modifier,
    )
}

/** The screen without its view model, so a screenshot can render it. */
@Composable
internal fun GoalDetailContent(
    state: GoalDetailUiState,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val goal = state.goal
    val stats = state.stats
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
                text = (goal?.title ?: ""),
                style = MaterialTheme.typography.headlineMedium,
                color = colors.ink,
                modifier = Modifier.weight(1f),
            )
        }

        if (goal != null && stats != null) {
            val description = goal.description
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.inkSoft,
                )
            }

            NeoStatRow(
                currentStreak = stats.currentStreak,
                bestStreak = stats.bestStreak,
                ratePercent = stats.completionRate.percent,
                rateLabel = stringResource(goal.recurrence.rateLabelRes()),
            )

            // Title, hint and grid are one block. At the column's 20.dp they read as
            // three unrelated things stacked.
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.detail_heatmap_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.ink,
                )
                Text(
                    text = stringResource(R.string.detail_heatmap_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSoft,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
                NeoHeatmap(
                    days = state.heatmap,
                    onDayClick = { day -> onDayClick(day.date) },
                )
            }

            NeoOutlineButton(
                text = stringResource(R.string.action_edit),
                onClick = { onEdit(goal.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/**
 * The rate window differs per recurrence, so the label has to as well — calling a
 * monthly goal's three-month rate "últimos 30 dias" would just be wrong.
 */
private fun Recurrence.rateLabelRes() = when (this) {
    Recurrence.DAILY -> R.string.stat_rate_daily
    Recurrence.WEEKLY -> R.string.stat_rate_weekly
    Recurrence.MONTHLY -> R.string.stat_rate_monthly
}
