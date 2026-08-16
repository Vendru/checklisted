package com.checklisted.app.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.checklisted.app.ui.theme.NeoTheme

/**
 * Renders a preview in both themes.
 *
 * The dark variant switches `uiMode` rather than passing `darkTheme = true`, so the
 * previewed tree resolves the theme exactly the way the running app does.
 */
@Preview(name = "claro", showBackground = true)
@Preview(name = "escuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
internal annotation class NeoPreviews

/** Preview-only helper: stacks samples on the theme background with breathing room. */
@Composable
internal fun PreviewStack(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    NeoTheme {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .background(NeoTheme.colors.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}
