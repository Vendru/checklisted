package com.checklisted.app.ui.components

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTeal
import com.checklisted.app.ui.theme.NeoTheme

/**
 * Container for a block of content. Optionally clickable — when [onClick] is null
 * the card is inert and carries no press treatment.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    color: Color = NeoTheme.colors.surface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()

    val clickModifier = if (onClick != null) {
        Modifier.neoClickable(interactionSource = interactionSource, role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .neoSurface(color = color, shape = NeoShapes.medium, pressed = pressed && onClick != null)
            .then(clickModifier)
            .padding(contentPadding),
        content = content,
    )
}

@Preview(name = "NeoCard claro", showBackground = true, backgroundColor = 0xFFFAF3E0)
@Composable
private fun NeoCardLightPreview() {
    NeoTheme(darkTheme = false) {
        PreviewStack { NeoCardSamples() }
    }
}

@Preview(name = "NeoCard escuro", showBackground = true, backgroundColor = 0xFF14120F)
@Composable
private fun NeoCardDarkPreview() {
    NeoTheme(darkTheme = true) {
        PreviewStack { NeoCardSamples() }
    }
}

@Composable
private fun NeoCardSamples() {
    NeoCard {
        Text("Beber 2L de água", style = MaterialTheme.typography.titleMedium)
        Text("Todo santo dia, sem desculpa.", style = MaterialTheme.typography.bodyMedium)
    }
    NeoCard(color = NeoTeal, onClick = {}) {
        Text(
            text = "Card clicável",
            style = MaterialTheme.typography.titleMedium,
            color = NeoTheme.colors.onAccent,
        )
    }
}
