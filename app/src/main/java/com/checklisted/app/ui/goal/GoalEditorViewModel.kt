package com.checklisted.app.ui.goal

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.repository.CompletionRepository
import com.checklisted.app.domain.repository.GoalRepository
import com.checklisted.app.ui.navigation.Destination
import com.checklisted.app.ui.theme.NeoAccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GoalEditorUiState(
    val goalId: String? = null,
    val title: String = "",
    val description: String = "",
    val recurrence: Recurrence = Recurrence.DAILY,
    /** What the goal is stored as, so a change away from it can be spotted. */
    val savedRecurrence: Recurrence? = null,
    /** How many completions the goal already has. Zero means nothing is at stake. */
    val completionCount: Int = 0,
    val accent: NeoAccent = NeoAccent.Default,
    val isArchived: Boolean = false,
    val showTitleError: Boolean = false,
    val isLoading: Boolean = false,
    val isFinished: Boolean = false,
) {
    val isEditing: Boolean = goalId != null
    val canSave: Boolean = title.isNotBlank()

    /**
     * Saving would take the goal's history off screen.
     *
     * Completions are stored under the key of the period they close — a daily goal
     * writes `2026-08-17`, a weekly one `2026-W34`. Change the recurrence and every
     * screen starts asking for keys of the new shape, so nothing matches: the streak
     * reads zero and the goal's comb goes blank. Nothing is deleted and switching back
     * restores the lot, but there is no way to tell that from the outside.
     *
     * Only true when there is something to lose sight of. Picking a different
     * recurrence for a goal created a minute ago costs nothing and warning about it
     * would be noise.
     */
    val recurrenceHidesHistory: Boolean =
        isEditing && completionCount > 0 && savedRecurrence != null && recurrence != savedRecurrence
}

@HiltViewModel
class GoalEditorViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
    private val completionRepository: CompletionRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val goalId: String? = savedStateHandle[Destination.GoalEditor.ARG_GOAL_ID]

    private val _uiState = MutableStateFlow(GoalEditorUiState(goalId = goalId, isLoading = goalId != null))
    val uiState: StateFlow<GoalEditorUiState> = _uiState.asStateFlow()

    init {
        if (goalId != null) {
            viewModelScope.launch {
                // A single read, not a subscription: the form is the user's draft from
                // here on, and later database writes must not overwrite what they type.
                val goal = goalRepository.observeGoal(goalId).first()
                if (goal == null) {
                    _uiState.update { it.copy(isLoading = false, isFinished = true) }
                } else {
                    // Also a single read: this only feeds a warning, and a live count
                    // would make the form twitch while the user is typing in it.
                    val completions = completionRepository.observeCompletionsForGoal(goalId).first()
                    _uiState.update {
                        it.copy(
                            title = goal.title,
                            description = goal.description.orEmpty(),
                            recurrence = goal.recurrence,
                            savedRecurrence = goal.recurrence,
                            completionCount = completions.size,
                            accent = NeoAccent.fromTag(goal.colorTag),
                            isArchived = goal.isArchived,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, showTitleError = false) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onRecurrenceChange(recurrence: Recurrence) {
        _uiState.update { it.copy(recurrence = recurrence) }
    }

    fun onAccentChange(accent: NeoAccent) {
        _uiState.update { it.copy(accent = accent) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) {
            _uiState.update { it.copy(showTitleError = true) }
            return
        }

        viewModelScope.launch {
            val existingId = state.goalId
            if (existingId == null) {
                goalRepository.createGoal(
                    title = state.title,
                    description = state.description.takeIf { it.isNotBlank() },
                    recurrence = state.recurrence,
                    colorTag = state.accent.name,
                )
            } else {
                val current = goalRepository.observeGoal(existingId).first()
                if (current != null) {
                    goalRepository.updateGoal(
                        current.copy(
                            title = state.title.trim(),
                            description = state.description.trim().takeIf { it.isNotBlank() },
                            recurrence = state.recurrence,
                            colorTag = state.accent.name,
                        ),
                    )
                }
            }
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    fun archive() {
        val id = _uiState.value.goalId ?: return
        viewModelScope.launch {
            goalRepository.setArchived(id, archived = !_uiState.value.isArchived)
            _uiState.update { it.copy(isFinished = true) }
        }
    }

    fun delete() {
        val id = _uiState.value.goalId ?: return
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
            _uiState.update { it.copy(isFinished = true) }
        }
    }
}
