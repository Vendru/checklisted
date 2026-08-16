package com.checklisted.app.ui.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.checklisted.app.R
import com.checklisted.app.ui.components.NeoButton
import com.checklisted.app.ui.components.NeoCard
import com.checklisted.app.ui.components.NeoCheckbox
import com.checklisted.app.ui.components.NeoChip
import com.checklisted.app.ui.components.NeoDialog
import com.checklisted.app.ui.components.NeoOutlineButton
import com.checklisted.app.ui.components.NeoProgressBar
import com.checklisted.app.ui.components.NeoTextField
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.condensed

/**
 * Living catalogue of the design system.
 *
 * This is the app's entry point until the Today screen exists, so every component
 * can be exercised on a real device — previews alone do not surface haptics or the
 * press travel.
 */
@Composable
fun ComponentGalleryScreen(modifier: Modifier = Modifier) {
    val colors = NeoTheme.colors
    var checkedOne by remember { mutableStateOf(true) }
    var checkedTwo by remember { mutableStateOf(false) }
    var recurrence by remember { mutableStateOf(0) }
    var title by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
        contentPadding = PaddingValues(
            top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding() + 20.dp,
            bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 32.dp,
            start = 20.dp,
            end = 20.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.gallery_title),
                style = MaterialTheme.typography.displaySmall.condensed(),
                color = colors.ink,
            )
        }

        item {
            GallerySection(title = stringResource(R.string.gallery_section_buttons)) {
                NeoButton(text = stringResource(R.string.action_new_goal), onClick = {})
                NeoButton(
                    text = stringResource(R.string.action_complete),
                    onClick = {},
                    accent = NeoAccent.TEAL,
                )
                NeoOutlineButton(text = stringResource(R.string.action_cancel), onClick = {})
                NeoButton(text = stringResource(R.string.action_disabled), onClick = {}, enabled = false)
            }
        }

        item {
            GallerySection(title = stringResource(R.string.gallery_section_checkbox)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NeoCheckbox(checked = checkedOne, onCheckedChange = { checkedOne = it })
                    NeoCheckbox(
                        checked = checkedTwo,
                        onCheckedChange = { checkedTwo = it },
                        accent = NeoAccent.PINK,
                    )
                    NeoCheckbox(
                        checked = checkedOne,
                        onCheckedChange = { checkedOne = it },
                        accent = NeoAccent.PURPLE,
                        size = 44.dp,
                    )
                }
            }
        }

        item {
            GallerySection(title = stringResource(R.string.gallery_section_chips)) {
                val labels = listOf(
                    stringResource(R.string.recurrence_daily),
                    stringResource(R.string.recurrence_weekly),
                    stringResource(R.string.recurrence_monthly),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    labels.forEachIndexed { index, label ->
                        NeoChip(
                            label = label,
                            selected = recurrence == index,
                            onClick = { recurrence = index },
                            accent = NeoAccent.entries[index],
                        )
                    }
                }
            }
        }

        item {
            GallerySection(title = stringResource(R.string.gallery_section_progress)) {
                NeoProgressBar(progress = 0.2f, accent = NeoAccent.YELLOW)
                NeoProgressBar(progress = 0.6f, accent = NeoAccent.TEAL)
                NeoProgressBar(progress = 1f, accent = NeoAccent.PINK)
            }
        }

        item {
            GallerySection(title = stringResource(R.string.gallery_section_text_field)) {
                NeoTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = stringResource(R.string.field_title),
                    placeholder = stringResource(R.string.field_title_placeholder),
                )
            }
        }

        item {
            GallerySection(title = stringResource(R.string.gallery_section_cards)) {
                NeoCard {
                    Text(
                        text = stringResource(R.string.sample_goal_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.ink,
                    )
                    Text(
                        text = stringResource(R.string.sample_goal_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.ink,
                    )
                }
                NeoButton(
                    text = stringResource(R.string.action_open_dialog),
                    onClick = { showDialog = true },
                    accent = NeoAccent.ORANGE,
                )
            }
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

@Composable
private fun GallerySection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = NeoTheme.colors.ink,
            textAlign = TextAlign.Start,
        )
        content()
    }
}

@Preview(name = "Galeria clara", showBackground = true, heightDp = 1400)
@Composable
private fun ComponentGalleryLightPreview() {
    NeoTheme(darkTheme = false) { ComponentGalleryScreen() }
}

@Preview(name = "Galeria escura", showBackground = true, heightDp = 1400)
@Composable
private fun ComponentGalleryDarkPreview() {
    NeoTheme(darkTheme = true) { ComponentGalleryScreen() }
}
