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
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

/** Square icon button used as the up affordance on every secondary screen. */
@Composable
fun NeoBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()
    val label = stringResource(R.string.action_back)

    Row(
        modifier = modifier
            .neoSurface(color = NeoTheme.colors.surface, shape = NeoShapes.small, pressed = pressed)
            .neoClickable(interactionSource = interactionSource, onClick = onClick)
            .size(NeoTokens.MinTouchTarget)
            .semantics { contentDescription = label },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NeoIconBack()
    }
}

@NeoPreviews
@Composable
private fun NeoBackButtonPreview() {
    PreviewStack {
        NeoBackButton(onClick = {})
    }
}
