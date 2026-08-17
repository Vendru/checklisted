package com.checklisted.app.ui.components

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

/**
 * Compact single-choice toggle used for recurrence pickers and filters.
 *
 * The selected state is carried by the fill, and the unselected state keeps the
 * same border weight so the row never shifts.
 */
@Composable
fun NeoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: NeoAccent? = null,
    enabled: Boolean = true,
    compact: Boolean = false,
) {
    val colors = NeoTheme.colors
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .neoSurface(
                color = if (selected) (accent?.color ?: colors.action) else colors.surface,
                shape = NeoShapes.small,
                pressed = pressed,
                enabled = enabled,
                shadowOffset = 3.dp,
            )
            .neoSelectable(
                selected = selected,
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = NeoTokens.MinTouchTarget)
            // Compact trims the sides only, never the height: a chip in a grid of
            // twenty-four has to be narrow to fit, and shrinking the touch target to
            // buy that would be paying for the layout with the finger.
            .padding(horizontal = if (compact) 8.dp else 16.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.onAction else colors.ink,
        )
    }
}

@NeoPreviews
@Composable
private fun NeoChipPreview() {
    PreviewStack { NeoChipSamples() }
}

@Composable
internal fun NeoChipSamples() {
    var selected by remember { mutableIntStateOf(0) }
    val labels = listOf(
        stringResource(R.string.recurrence_daily),
        stringResource(R.string.recurrence_weekly),
        stringResource(R.string.recurrence_monthly),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEachIndexed { index, label ->
            NeoChip(
                label = label,
                selected = selected == index,
                onClick = { selected = index },
                accent = NeoAccent.entries[index],
            )
        }
    }
}
