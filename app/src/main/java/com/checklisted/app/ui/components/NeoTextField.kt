package com.checklisted.app.ui.components

import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.theme.NeoOrange
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens
import com.checklisted.app.ui.theme.NeoYellow

/**
 * Single- or multi-line text input.
 *
 * Focus is signalled by filling the field with the accent color rather than by a
 * hairline or a floating label — there is no low-contrast state anywhere in this
 * design system.
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
    enabled: Boolean = true,
    errorText: String? = null,
    imeAction: ImeAction = ImeAction.Done,
) {
    val colors = NeoTheme.colors
    val interactionSource = rememberNeoInteractionSource()
    val focused by interactionSource.collectIsFocusedAsState()

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = colors.ink,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            textStyle = LocalTextStyle.current.merge(
                MaterialTheme.typography.bodyLarge.copy(color = colors.ink),
            ),
            cursorBrush = SolidColor(colors.ink),
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            modifier = Modifier
                .fillMaxWidth()
                .neoSurface(
                    color = when {
                        errorText != null -> NeoOrange
                        focused -> NeoYellow
                        else -> colors.surface
                    },
                    shape = NeoShapes.small,
                    borderColor = colors.ink,
                )
                .defaultMinSize(minHeight = NeoTokens.MinTouchTarget)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.ink.copy(alpha = 0.6f),
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

@Preview(name = "NeoTextField claro", showBackground = true, backgroundColor = 0xFFFAF3E0)
@Composable
private fun NeoTextFieldLightPreview() {
    NeoTheme(darkTheme = false) {
        PreviewStack { NeoTextFieldSamples() }
    }
}

@Preview(name = "NeoTextField escuro", showBackground = true, backgroundColor = 0xFF14120F)
@Composable
private fun NeoTextFieldDarkPreview() {
    NeoTheme(darkTheme = true) {
        PreviewStack { NeoTextFieldSamples() }
    }
}

@Composable
private fun NeoTextFieldSamples() {
    NeoTextField(value = "", onValueChange = {}, label = "Título", placeholder = "Ex.: Ler 20 páginas")
    NeoTextField(value = "Correr 5 km", onValueChange = {}, label = "Título")
    NeoTextField(
        value = "",
        onValueChange = {},
        label = "Título",
        placeholder = "Ex.: Ler 20 páginas",
        errorText = "Dá pra escrever alguma coisa aí.",
    )
    NeoTextField(
        value = "Sem pressa, mas sem pausa.",
        onValueChange = {},
        label = "Descrição",
        singleLine = false,
        minLines = 3,
    )
}
