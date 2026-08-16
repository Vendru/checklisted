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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens
import com.checklisted.app.ui.theme.displayUppercase

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
    NeoButtonBody(
        text = text,
        onClick = onClick,
        modifier = modifier,
        fill = if (enabled) accent.color else colors.surfaceMuted,
        contentColor = if (enabled) colors.onAccent else colors.ink,
        enabled = enabled,
        leadingIcon = leadingIcon,
    )
}

/** Lower-emphasis variant: same body, neutral fill. */
@Composable
fun NeoOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fill: Color = NeoTheme.colors.surface,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    NeoButtonBody(
        text = text,
        onClick = onClick,
        modifier = modifier,
        fill = fill,
        contentColor = NeoTheme.colors.ink,
        enabled = enabled,
        leadingIcon = leadingIcon,
    )
}

/**
 * The one button body.
 *
 * Both public variants route through here so padding, touch target and the disabled
 * treatment can only ever be changed in one place.
 */
@Composable
private fun NeoButtonBody(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    fill: Color,
    contentColor: Color,
    enabled: Boolean,
    leadingIcon: (@Composable () -> Unit)?,
) {
    val interactionSource = rememberNeoInteractionSource()
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = modifier
            .neoSurface(
                color = fill,
                shape = NeoShapes.small,
                pressed = pressed,
                enabled = enabled,
            )
            .neoClickable(interactionSource = interactionSource, enabled = enabled, onClick = onClick)
            .defaultMinSize(minHeight = NeoTokens.MinTouchTarget)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingIcon?.invoke()
        Text(
            text = text.displayUppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

@NeoPreviews
@Composable
private fun NeoButtonPreview() {
    PreviewStack { NeoButtonSamples() }
}

@Composable
internal fun NeoButtonSamples() {
    NeoButton(text = stringResource(R.string.action_new_goal), onClick = {})
    NeoButton(text = stringResource(R.string.action_complete), onClick = {}, accent = NeoAccent.TEAL)
    NeoOutlineButton(text = stringResource(R.string.action_cancel), onClick = {})
    NeoButton(text = stringResource(R.string.action_disabled), onClick = {}, enabled = false)
    NeoOutlineButton(text = stringResource(R.string.action_disabled), onClick = {}, enabled = false)
}
