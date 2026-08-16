package com.checklisted.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.condensed
import com.checklisted.app.ui.theme.displayUppercase

/**
 * A single number with its label.
 *
 * The value and the label are merged into one semantics node, so a screen reader
 * announces "sequência atual, 12" instead of two unrelated fragments.
 */
@Composable
fun NeoStatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    color: Color = NeoTheme.colors.surface,
) {
    val colors = NeoTheme.colors
    val spoken = "$label: $value"

    NeoCard(
        modifier = modifier.clearAndSetSemantics { contentDescription = spoken },
        color = color,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(14.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.displaySmall.condensed(),
            color = colors.ink,
        )
        Text(
            text = label.displayUppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = colors.ink,
        )
    }
}

/** The three stats shown side by side on a goal's detail screen. */
@Composable
fun NeoStatRow(
    currentStreak: Int,
    bestStreak: Int,
    ratePercent: Int,
    rateLabel: String,
    modifier: Modifier = Modifier,
    accent: NeoAccent = NeoAccent.Default,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        NeoStatTile(
            value = currentStreak.toString(),
            label = stringResource(R.string.stat_current_streak),
            color = accent.color,
            modifier = Modifier.weight(1f),
        )
        NeoStatTile(
            value = bestStreak.toString(),
            label = stringResource(R.string.stat_best_streak),
            modifier = Modifier.weight(1f),
        )
        NeoStatTile(
            value = "$ratePercent%",
            label = rateLabel,
            modifier = Modifier.weight(1f),
        )
    }
}

@NeoPreviews
@Composable
private fun NeoStatRowPreview() {
    PreviewStack {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NeoStatRow(
                currentStreak = 12,
                bestStreak = 31,
                ratePercent = 73,
                rateLabel = stringResource(R.string.stat_rate_daily),
                accent = NeoAccent.PINK,
            )
        }
    }
}
