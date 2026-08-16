package com.checklisted.app.ui.components

import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

/**
 * Single- or multi-line text input.
 *
 * Focus is signalled by filling the field with the accent color rather than by a
 * hairline or a floating label — there is no low-contrast state anywhere in this
 * design system. The text color follows the fill for the same reason: on an accent
 * ground it switches to `onAccent`, or bone-on-yellow would be unreadable in the
 * dark theme.
 */
@Composable
fun NeoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Done,
) {
    val colors = NeoTheme.colors
    val interactionSource = rememberNeoInteractionSource()
    val focused by interactionSource.collectIsFocusedAsState()

    // A caller asking for several lines cannot also be single-line. Reconciling it
    // here rather than trusting the call site: BasicTextField hands minLines and
    // maxLines straight to validateMinMaxLines, which throws when minLines wins.
    val multiline = !singleLine || minLines > 1
    val resolvedMaxLines = maxOf(maxLines, minLines)

    val onAccentFill = errorText != null || focused
    val contentColor = if (onAccentFill) colors.onAction else colors.ink

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.ink,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = !multiline,
            minLines = minLines,
            maxLines = resolvedMaxLines,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = contentColor),
            cursorBrush = SolidColor(contentColor),
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            modifier = Modifier
                .fillMaxWidth()
                .neoSurface(
                    color = when {
                        // Focus and error both fill with the action colour; the
                        // error is told apart by the message under the field, not by
                        // a second hue competing with it.
                        errorText != null || focused -> colors.action
                        else -> colors.surface
                    },
                    shape = NeoShapes.small,
                    enabled = enabled,
                )
                .defaultMinSize(minHeight = NeoTokens.MinTouchTarget)
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .semantics {
                    // The label is a sibling node, so without this the field has no
                    // accessible name and the error is conveyed by fill color alone.
                    if (label != null) contentDescription = label
                    if (errorText != null) error(errorText)
                },
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = contentColor.copy(alpha = 0.6f),
                        )
                    }
                    innerTextField()
                }
            },
        )

        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.bodySmall,
                color = colors.ink,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
    }
}

@NeoPreviews
@Composable
private fun NeoTextFieldPreview() {
    PreviewStack { NeoTextFieldSamples() }
}

@Composable
internal fun NeoTextFieldSamples() {
    NeoTextField(
        value = "",
        onValueChange = {},
        label = stringResource(R.string.field_title),
        placeholder = stringResource(R.string.field_title_placeholder),
    )
    NeoTextField(
        value = stringResource(R.string.sample_goal_run),
        onValueChange = {},
        label = stringResource(R.string.field_title),
    )
    NeoTextField(
        value = "",
        onValueChange = {},
        label = stringResource(R.string.field_title),
        placeholder = stringResource(R.string.field_title_placeholder),
        errorText = stringResource(R.string.error_title_required),
    )
    NeoTextField(
        value = stringResource(R.string.sample_goal_description),
        onValueChange = {},
        label = stringResource(R.string.field_description),
        singleLine = false,
        minLines = 3,
    )
}
