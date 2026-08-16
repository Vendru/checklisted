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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.displayUppercase

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
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        NeoDialogContent(
            title = title,
            modifier = modifier.padding(24.dp),
            message = message,
            confirmText = confirmText,
            onConfirm = onConfirm,
            dismissText = dismissText,
            onDismiss = onDismissRequest,
            confirmAccent = confirmAccent,
            content = content,
        )
    }
}

/**
 * The dialog container, minus the window.
 *
 * Extracted so the preview renders the real thing — Android Studio's preview surface
 * does not host actual [Dialog] windows, and a hand-copied stand-in would drift from
 * the shipped spacing.
 */
@Composable
private fun NeoDialogContent(
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismiss: () -> Unit = {},
    confirmAccent: NeoAccent = NeoAccent.ORANGE,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = NeoTheme.colors

    Column(
        modifier = modifier
            .widthIn(max = 380.dp)
            .neoSurface(color = colors.surface, shape = NeoShapes.medium)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title.displayUppercase(),
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
                    NeoOutlineButton(text = dismissText, onClick = onDismiss)
                }
                if (confirmText != null && onConfirm != null) {
                    NeoButton(text = confirmText, onClick = onConfirm, accent = confirmAccent)
                }
            }
        }
    }
}

@NeoPreviews
@Composable
private fun NeoDialogPreview() {
    PreviewStack {
        NeoDialogContent(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = {},
        )
    }
}
