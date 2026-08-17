package com.checklisted.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.checklisted.app.R
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoShapes
import com.checklisted.app.ui.theme.NeoTheme

private val DialogMaxWidth = 380.dp
private val DialogMargin = 24.dp

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
    confirmAccent: NeoAccent? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    // Turning off the platform width means nothing stops the box from growing past the
    // display: 380.dp of content inside 24.dp of margin needs 428.dp, and the narrow
    // phones this app targets are 360.dp across, so the closing button was rendered off
    // the right edge. The width comes from the screen rather than from the incoming
    // constraints, which a dialog window does not reliably supply — and it is a fixed
    // width, not a maximum, because a wrap-content window measured the text against the
    // cap and then placed the box narrower, slicing the last letter off every line.
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val width = minOf(DialogMaxWidth, screenWidth - DialogMargin * 2)

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        NeoDialogContent(
            title = title,
            modifier = modifier,
            width = width,
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
 * Extracted so the preview and the screenshots render the real thing — neither Android
 * Studio's preview surface nor Paparazzi hosts an actual [Dialog] window at the size a
 * device gives it, and a hand-copied stand-in would drift from the shipped spacing.
 */
@Composable
internal fun NeoDialogContent(
    title: String,
    modifier: Modifier = Modifier,
    width: Dp = DialogMaxWidth,
    message: String? = null,
    confirmText: String? = null,
    onConfirm: (() -> Unit)? = null,
    dismissText: String? = null,
    onDismiss: () -> Unit = {},
    confirmAccent: NeoAccent? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val colors = NeoTheme.colors

    Column(
        modifier = modifier
            .width(width)
            .neoSurface(color = colors.surface, shape = NeoShapes.medium)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
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
