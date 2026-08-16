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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme

/**
 * Container for a block of content.
 *
 * Split into two bodies by whether it is clickable: an inert card is the common
 * case (goal rows, empty states, stat blocks) and must not pay for an interaction
 * source, a press collector and an idle animation that can never run.
 */
@Composable
fun NeoCard(
    modifier: Modifier = Modifier,
    color: Color = NeoTheme.colors.surface,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (onClick == null) {
        Column(
            modifier = modifier
                .neoSurface(color = color, shape = NeoShapes.medium)
                .padding(contentPadding),
            content = content,
        )
    } else {
        ClickableNeoCard(
            modifier = modifier,
            color = color,
            contentPadding = contentPadding,
            onClick = onClick,
            content = content,
        )
    }
}

@Composable
private fun ClickableNeoCard(
    modifier: Modifier,
    color: Color,
    contentPadding: PaddingValues,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()

    Column(
        modifier = modifier
            .neoSurface(color = color, shape = NeoShapes.medium, pressed = pressed)
            .neoClickable(interactionSource = interactionSource, role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        content = content,
    )
}

@NeoPreviews
@Composable
private fun NeoCardPreview() {
    PreviewStack { NeoCardSamples() }
}

@Composable
internal fun NeoCardSamples() {
    NeoCard {
        // No explicit color: this is the case that used to fall back to
        // Color.Black before NeoTheme started providing LocalContentColor.
        Text(
            text = stringResource(R.string.sample_goal_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(R.string.sample_goal_description),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    NeoCard(color = NeoTheme.colors.action, onClick = {}) {
        Text(
            text = stringResource(R.string.sample_card_clickable),
            style = MaterialTheme.typography.titleMedium,
            color = NeoTheme.colors.onAction,
        )
    }
}
