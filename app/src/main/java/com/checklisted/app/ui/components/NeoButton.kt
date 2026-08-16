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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

/**
 * Primary action. Label is always uppercase — the design system has no
 * sentence-case buttons.
 */
@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: NeoAccent = NeoAccent.Default,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = NeoTheme.colors
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .neoSurface(
                color = if (enabled) accent.color else colors.surfaceMuted,
                shape = NeoShapes.small,
                pressed = pressed,
                enabled = enabled,
            )
            .neoClickable(interactionSource = interactionSource, enabled = enabled, onClick = onClick)
            .defaultMinSize(minHeight = NeoTokens.MinTouchTarget)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.55f },
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke()
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) colors.onAccent else colors.ink,
        )
    }
}

/** Lower-emphasis variant: same structure, neutral fill. */
@Composable
fun NeoOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fill: Color = NeoTheme.colors.surface,
) {
    val colors = NeoTheme.colors
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .neoSurface(color = fill, shape = NeoShapes.small, pressed = pressed, enabled = enabled)
            .neoClickable(interactionSource = interactionSource, enabled = enabled, onClick = onClick)
            .defaultMinSize(minHeight = NeoTokens.MinTouchTarget)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .graphicsLayer { alpha = if (enabled) 1f else 0.55f },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = colors.ink,
        )
    }
}

@Preview(name = "NeoButton claro", showBackground = true, backgroundColor = 0xFFFAF3E0)
@Composable
private fun NeoButtonLightPreview() {
    NeoTheme(darkTheme = false) {
        PreviewStack {
            NeoButton(text = "Nova meta", onClick = {})
            NeoButton(text = "Concluir", onClick = {}, accent = NeoAccent.TEAL)
            NeoButton(text = "Desativado", onClick = {}, enabled = false)
            NeoOutlineButton(text = "Cancelar", onClick = {})
        }
    }
}

@Preview(name = "NeoButton escuro", showBackground = true, backgroundColor = 0xFF14120F)
@Composable
private fun NeoButtonDarkPreview() {
    NeoTheme(darkTheme = true) {
        PreviewStack {
            NeoButton(text = "Nova meta", onClick = {})
            NeoButton(text = "Concluir", onClick = {}, accent = NeoAccent.TEAL)
            NeoButton(text = "Desativado", onClick = {}, enabled = false)
            NeoOutlineButton(text = "Cancelar", onClick = {})
        }
    }
}
