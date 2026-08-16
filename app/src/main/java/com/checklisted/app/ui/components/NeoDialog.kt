package com.checklisted.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme

/**
 * Modal used for destructive confirmations and small pickers.
 *
 * Built on [Dialog] rather than Material's `AlertDialog` so the container is a plain
 * bordered box with a hard shadow, with no elevation tint or rounded scrim.
 */
@Composable
fun NeoDialog(
    title: String,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null,
    confirmAccent: NeoAccent = NeoAccent.ORANGE,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = NeoTheme.colors

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = modifier
                .padding(24.dp)
                .widthIn(max = 380.dp)
                .neoSurface(color = colors.surface, shape = NeoShapes.medium)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.headlineSmall,
                color = colors.ink,
            )

            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.ink,
                )
            }

            content?.invoke(this)

            if (confirmText != null || dismissText != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dismissText != null) {
                        NeoOutlineButton(text = dismissText, onClick = onDismissRequest)
                    }
                    if (confirmText != null && onConfirm != null) {
                        NeoButton(text = confirmText, onClick = onConfirm, accent = confirmAccent)
                    }
                }
            }
        }
    }
}

@Preview(name = "NeoDialog claro", showBackground = true, backgroundColor = 0xFFFAF3E0)
@Composable
private fun NeoDialogLightPreview() {
    NeoTheme(darkTheme = false) {
        PreviewStack { NeoDialogBodyPreview() }
    }
}

@Preview(name = "NeoDialog escuro", showBackground = true, backgroundColor = 0xFF14120F)
@Composable
private fun NeoDialogDarkPreview() {
    NeoTheme(darkTheme = true) {
        PreviewStack { NeoDialogBodyPreview() }
    }
}

/**
 * Previews render the dialog body inline: Android Studio's preview surface does not
 * host real [Dialog] windows, so the container is what needs reviewing here.
 */
@Composable
private fun NeoDialogBodyPreview() {
    val colors = NeoTheme.colors
    Column(
        modifier = Modifier
            .widthIn(max = 380.dp)
            .neoSurface(color = colors.surface, shape = NeoShapes.medium)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "EXCLUIR META?",
            style = MaterialTheme.typography.headlineSmall,
            color = colors.ink,
        )
        Text(
            text = "Isso apaga o histórico junto. Se você só quer tirar da lista, arquive.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoOutlineButton(text = "Cancelar", onClick = {})
            NeoButton(text = "Excluir", onClick = {}, accent = NeoAccent.ORANGE)
        }
    }
}
