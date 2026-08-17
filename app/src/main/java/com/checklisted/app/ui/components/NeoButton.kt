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

/**
 * Primary action. Label is always uppercase — the design system has no
 * sentence-case buttons.
 */
@Composable
fun NeoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: NeoAccent? = null,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    // Null means the app's single action colour. A goal tag can still be passed to
    // colour an action that belongs to one specific goal.
    NeoButtonBody(
        text = text,
        onClick = onClick,
        modifier = modifier,
        fill = accent?.color ?: NeoTheme.colors.action,
        contentColor = if (accent == null) NeoTheme.colors.onAction else NeoTheme.colors.surface,
        enabled = enabled,
        leadingIcon = leadingIcon,
    )
}

/**
 * The button that destroys something.
 *
 * Its own variant rather than a colour passed in, so every irreversible confirmation
 * in the app is the same crimson and no caller can quietly hand it a friendlier one.
 */
@Composable
fun NeoDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    NeoButtonBody(
        text = text,
        onClick = onClick,
        modifier = modifier,
        fill = NeoTheme.colors.danger,
        contentColor = NeoTheme.colors.onDanger,
        enabled = enabled,
        leadingIcon = null,
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
    val colors = NeoTheme.colors
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
            text = text,
            style = MaterialTheme.typography.labelLarge,
            // Disabled swaps the fill to a neutral, so the label follows the ink
            // rather than the accent's contrast colour.
            color = if (enabled) contentColor else colors.ink.copy(alpha = NeoTokens.DISABLED_CONTENT_ALPHA),
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
