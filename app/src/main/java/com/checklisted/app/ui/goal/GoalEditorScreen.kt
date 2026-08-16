package com.checklisted.app.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checklisted.app.R
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.ui.components.NeoBackButton
import com.checklisted.app.ui.components.NeoButton
import com.checklisted.app.ui.components.NeoCheckbox
import com.checklisted.app.ui.components.NeoChip
import com.checklisted.app.ui.components.NeoDialog
import com.checklisted.app.ui.components.NeoOutlineButton
import com.checklisted.app.ui.components.NeoTextField
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.condensed
import com.checklisted.app.ui.theme.displayUppercase

@Composable
fun GoalEditorScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GoalEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onDone()
    }

    GoalEditorContent(
        state = state,
        onTitleChange = viewModel::onTitleChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onRecurrenceChange = viewModel::onRecurrenceChange,
        onAccentChange = viewModel::onAccentChange,
        onSave = viewModel::save,
        onArchive = viewModel::archive,
        onDelete = viewModel::delete,
        onBack = onDone,
        modifier = modifier,
    )
}

@Composable
private fun GoalEditorContent(
    state: GoalEditorUiState,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onRecurrenceChange: (Recurrence) -> Unit,
    onAccentChange: (NeoAccent) -> Unit,
    onSave: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = NeoTheme.colors
    var showDeleteDialog by remember { mutableStateOf(false) }

    val insets = WindowInsets.systemBars
        .add(WindowInsets(left = 20.dp, top = 20.dp, right = 20.dp, bottom = 32.dp))
        .asPaddingValues()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(insets),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NeoBackButton(onClick = onBack)
            Text(
                text = stringResource(
                    if (state.isEditing) R.string.goal_editor_title_edit else R.string.goal_editor_title_new,
                ).displayUppercase(),
                style = MaterialTheme.typography.headlineMedium.condensed(),
                color = colors.ink,
            )
        }

        NeoTextField(
            value = state.title,
            onValueChange = onTitleChange,
            label = stringResource(R.string.field_title),
            placeholder = stringResource(R.string.field_title_placeholder),
            imeAction = ImeAction.Next,
            errorText = stringResource(R.string.error_title_required).takeIf { state.showTitleError },
        )

        NeoTextField(
            value = state.description,
            onValueChange = onDescriptionChange,
            label = stringResource(R.string.field_description),
            placeholder = stringResource(R.string.field_description_placeholder),
            singleLine = false,
            minLines = 3,
        )

        FieldLabel(text = stringResource(R.string.field_recurrence))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Recurrence.entries.forEach { recurrence ->
                NeoChip(
                    label = stringResource(recurrence.labelRes()),
                    selected = state.recurrence == recurrence,
                    onClick = { onRecurrenceChange(recurrence) },
                    accent = state.accent,
                )
            }
        }

        FieldLabel(text = stringResource(R.string.field_color))
        AccentPicker(selected = state.accent, onSelect = onAccentChange)

        NeoButton(
            text = stringResource(R.string.action_save),
            onClick = onSave,
            accent = state.accent,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth(),
        )

        if (state.isEditing) {
            NeoOutlineButton(
                text = stringResource(
                    if (state.isArchived) R.string.action_unarchive else R.string.action_archive,
                ),
                onClick = onArchive,
                modifier = Modifier.fillMaxWidth(),
            )
            NeoButton(
                text = stringResource(R.string.action_delete),
                onClick = { showDeleteDialog = true },
                accent = NeoAccent.ORANGE,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }

    if (showDeleteDialog) {
        NeoDialog(
            title = stringResource(R.string.dialog_delete_title),
            message = stringResource(R.string.dialog_delete_message),
            confirmText = stringResource(R.string.action_delete),
            dismissText = stringResource(R.string.action_cancel),
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismissRequest = { showDeleteDialog = false },
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text.displayUppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = NeoTheme.colors.ink,
    )
}

/** Colour tag picker. Each swatch is a filled checkbox, so selection reads at a glance. */
@Composable
private fun AccentPicker(selected: NeoAccent, onSelect: (NeoAccent) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        NeoAccent.entries.forEach { accent ->
            NeoCheckbox(
                checked = accent == selected,
                onCheckedChange = { onSelect(accent) },
                accent = accent,
                contentDescription = stringResource(accent.labelRes()),
            )
        }
    }
}

private fun Recurrence.labelRes() = when (this) {
    Recurrence.DAILY -> R.string.recurrence_daily
    Recurrence.WEEKLY -> R.string.recurrence_weekly
    Recurrence.MONTHLY -> R.string.recurrence_monthly
}

private fun NeoAccent.labelRes() = when (this) {
    NeoAccent.YELLOW -> R.string.color_yellow
    NeoAccent.PINK -> R.string.color_pink
    NeoAccent.TEAL -> R.string.color_teal
    NeoAccent.ORANGE -> R.string.color_orange
    NeoAccent.PURPLE -> R.string.color_purple
}
