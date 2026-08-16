package com.checklisted.app.ui.components

import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

/**
 * Square icon button, sized to the minimum touch target.
 *
 * Icon-only, so [contentDescription] is the node's whole accessible name and is
 * required rather than optional.
 */
@Composable
fun NeoIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .neoSurface(color = NeoTheme.colors.surface, shape = NeoShapes.small, pressed = pressed)
            .neoClickable(interactionSource = interactionSource, onClick = onClick)
            .size(NeoTokens.MinTouchTarget)
            .semantics { this.contentDescription = contentDescription },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        icon()
    }
}

/** The up affordance on every secondary screen. */
@Composable
fun NeoBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NeoIconButton(
        onClick = onClick,
        contentDescription = stringResource(R.string.action_back),
        modifier = modifier,
    ) {
        NeoIconBack()
    }
}

@NeoPreviews
@Composable
private fun NeoIconButtonPreview() {
    PreviewStack {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NeoBackButton(onClick = {})
            NeoIconButton(onClick = {}, contentDescription = "Configurações") { NeoIconSettings() }
        }
    }
}
