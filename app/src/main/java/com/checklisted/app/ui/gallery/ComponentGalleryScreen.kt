package com.checklisted.app.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.components.NeoButton
import com.checklisted.app.ui.components.NeoButtonSamples
import com.checklisted.app.ui.components.NeoCardSamples
import com.checklisted.app.ui.components.NeoCheckboxSamples
import com.checklisted.app.ui.components.NeoChipSamples
import com.checklisted.app.ui.components.NeoDialog
import com.checklisted.app.ui.components.NeoProgressBarSamples
import com.checklisted.app.ui.components.NeoTextFieldSamples
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.condensed
import com.checklisted.app.ui.theme.displayUppercase

/**
 * Living catalogue of the design system.
 *
 * This is the app's entry point until the Today screen exists, so every component
 * can be exercised on a real device — previews alone do not surface haptics or the
 * press travel. Each section reuses the same sample composables the component
 * previews render, so a new variant is demoed once rather than in two places that
 * can disagree.
 */
@Composable
fun ComponentGalleryScreen(modifier: Modifier = Modifier) {
    val colors = NeoTheme.colors
    var showDialog by remember { mutableStateOf(false) }

    // All four edges: under edge-to-edge the horizontal insets are what keep
    // content clear of the navigation bar and cutouts in landscape.
    val insets = WindowInsets.systemBars
        .add(WindowInsets(left = 20.dp, top = 20.dp, right = 20.dp, bottom = 32.dp))
        .asPaddingValues()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            // enableEdgeToEdge() turns off decorFitsSystemWindows, so adjustResize
            // no longer shrinks the window when the keyboard opens.
            .imePadding(),
        contentPadding = insets,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.gallery_title),
                style = MaterialTheme.typography.displaySmall.condensed(),
                color = colors.ink,
            )
        }

        gallerySection(R.string.gallery_section_buttons) { NeoButtonSamples() }
        gallerySection(R.string.gallery_section_checkbox) { NeoCheckboxSamples() }
        gallerySection(R.string.gallery_section_chips) { NeoChipSamples() }
        gallerySection(R.string.gallery_section_progress) { NeoProgressBarSamples() }
        gallerySection(R.string.gallery_section_text_field) { NeoTextFieldSamples() }
        gallerySection(R.string.gallery_section_cards) {
            NeoCardSamples()
            NeoButton(
                text = stringResource(R.string.action_open_dialog),
                onClick = { showDialog = true },
                accent = NeoAccent.ORANGE,
            )
        }
    }

    if (showDialog) {
        NeoDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = { showDialog = false },
            onDismissRequest = { showDialog = false },
        )
    }
}

/** One titled section, emitted as its own lazy item. */
private fun LazyListScope.gallerySection(
    titleRes: Int,
    content: @Composable () -> Unit,
) = item {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(titleRes).displayUppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = NeoTheme.colors.ink,
        )
        content()
    }
}

@Preview(name = "Galeria", showBackground = true, heightDp = 1600)
@Composable
private fun ComponentGalleryPreview() {
    NeoTheme { ComponentGalleryScreen() }
}
