package com.checklisted.app.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.checklisted.app.R
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.ui.components.NeoBackButton
import com.checklisted.app.ui.components.NeoButton
import com.checklisted.app.ui.components.NeoCard
import com.checklisted.app.ui.components.NeoChip
import com.checklisted.app.ui.components.NeoDangerButton
import com.checklisted.app.ui.components.NeoDialog
import com.checklisted.app.ui.components.NeoIconCheck
import com.checklisted.app.ui.components.NeoOutlineButton
import com.checklisted.app.ui.components.NeoTextField
import com.checklisted.app.ui.components.neoSelectable
import com.checklisted.app.ui.components.neoSurface
import com.checklisted.app.ui.components.readableWidth
import com.checklisted.app.ui.components.rememberNeoInteractionSource
import com.checklisted.app.ui.theme.NeoAccent
import com.checklisted.app.ui.theme.NeoCombCell
import com.checklisted.app.ui.theme.NeoTheme
import com.checklisted.app.ui.theme.NeoTokens

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

/** The screen without its view model, so a screenshot can render it. */
@Composable
internal fun GoalEditorContent(
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
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val insets = WindowInsets.systemBars
        .add(WindowInsets(left = 20.dp, top = 20.dp, right = 20.dp, bottom = 32.dp))
        .asPaddingValues()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .readableWidth()
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
                ),
                style = MaterialTheme.typography.headlineMedium,
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
                )
            }
        }

        if (state.recurrenceHidesHistory && state.savedRecurrence != null) {
            RecurrenceWarning(
                completionCount = state.completionCount,
                savedRecurrence = state.savedRecurrence,
            )
        }

        FieldLabel(text = stringResource(R.string.field_color))
        AccentPicker(selected = state.accent, onSelect = onAccentChange)

        NeoButton(
            text = stringResource(R.string.action_save),
            onClick = onSave,
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
            // Crimson, like the confirmation it opens and like the swipe drawer on the
            // Today list. It was honey — the same fill as Save, directly under it.
            NeoDangerButton(
                text = stringResource(R.string.action_delete),
                onClick = { showDeleteDialog = true },
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
            destructive = true,
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismissRequest = { showDeleteDialog = false },
        )
    }
}

/**
 * What changing the recurrence of a goal with history actually does.
 *
 * Wax rather than crimson: the change is reversible and reversing it restores
 * everything, so this is a heads-up and not a destructive confirmation. Naming the
 * number of marks and the way back is the part that matters — the alarming version of
 * this moment is opening the goal afterwards and finding a blank comb with no
 * explanation anywhere.
 */
@Composable
private fun RecurrenceWarning(completionCount: Int, savedRecurrence: Recurrence) {
    val colors = NeoTheme.colors

    NeoCard(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surfaceMuted,
    ) {
        Text(
            text = stringResource(R.string.editor_recurrence_warning_title),
            style = MaterialTheme.typography.labelLarge,
            color = colors.ink,
        )
        Text(
            text = pluralStringResource(
                R.plurals.editor_recurrence_warning,
                completionCount,
                completionCount,
                stringResource(savedRecurrence.labelRes()),
            ),
            style = MaterialTheme.typography.bodySmall,
            color = colors.ink,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = NeoTheme.colors.ink,
    )
}

/**
 * Colour tag picker: five comb cells, each wearing its own colour.
 *
 * Built on checkboxes before, which fill with the tag colour only while checked — so
 * four of the five swatches were blank white and the one thing a colour picker has to
 * show, the colours, was the one thing it did not. The tick is ink on every one of
 * them, which clears 3:1 on all five; white did not, on yellow or teal.
 */
@Composable
private fun AccentPicker(selected: NeoAccent, onSelect: (NeoAccent) -> Unit) {
    val colors = NeoTheme.colors

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NeoAccent.entries.forEach { accent ->
            val interactionSource = rememberNeoInteractionSource()
            val pressed by interactionSource.collectIsPressedAsState()
            val isSelected = accent == selected
            val label = stringResource(accent.labelRes())

            Box(
                modifier = Modifier
                    .neoSurface(
                        color = accent.color,
                        shape = NeoCombCell,
                        pressed = pressed,
                    )
                    .neoSelectable(
                        selected = isSelected,
                        interactionSource = interactionSource,
                    ) { onSelect(accent) }
                    .size(NeoTokens.MinTouchTarget)
                    .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                if (isSelected) {
                    NeoIconCheck(tint = colors.onAction, size = 20.dp)
                }
            }
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
