package com.checklisted.app.ui.components

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme

/**
 * Compact toggle used for recurrence pickers and filters. The selected state is
 * carried by the fill, and the unselected state keeps the same border weight so
 * the row never shifts.
 */
@Composable
fun NeoChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: NeoAccent = NeoAccent.Default,
    enabled: Boolean = true,
) {
    val colors = NeoTheme.colors
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .neoSurface(
                color = if (selected) accent.color else colors.surface,
                shape = NeoShapes.small,
                pressed = pressed,
                enabled = enabled,
                shadowOffset = 3.dp,
            )
            .neoClickable(
                interactionSource = interactionSource,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) colors.onAccent else colors.ink,
        )
    }
}

@Preview(name = "NeoChip claro", showBackground = true, backgroundColor = 0xFFFAF3E0)
@Composable
private fun NeoChipLightPreview() {
    NeoTheme(darkTheme = false) {
        PreviewStack { NeoChipSamples() }
    }
}

@Preview(name = "NeoChip escuro", showBackground = true, backgroundColor = 0xFF14120F)
@Composable
private fun NeoChipDarkPreview() {
    NeoTheme(darkTheme = true) {
        PreviewStack { NeoChipSamples() }
    }
}

@Composable
private fun NeoChipSamples() {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NeoChip(label = "Diária", selected = true, onClick = {})
        NeoChip(label = "Semanal", selected = false, onClick = {})
        NeoChip(label = "Mensal", selected = false, onClick = {})
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NeoChip(label = "Semanal", selected = true, onClick = {}, accent = NeoAccent.PURPLE)
        NeoChip(label = "Mensal", selected = true, onClick = {}, accent = NeoAccent.ORANGE)
    }
}
