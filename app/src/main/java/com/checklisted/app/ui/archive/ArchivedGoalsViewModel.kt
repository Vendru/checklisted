package com.checklisted.app.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ArchivedGoalsUiState(
    val goals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
)

/**
 * The way back out of the archive.
 *
 * Archiving used to be one-way: the Today list asks for unarchived goals only, the
 * editor is reachable only from a row of that list, and the unarchive button lives in
 * the editor. Every piece worked and the loop was still closed.
 */
@HiltViewModel
class ArchivedGoalsViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
) : ViewModel() {
    val uiState: StateFlow<ArchivedGoalsUiState> =
        goalRepository.observeGoals(includeArchived = true)
            .map { goals ->
                ArchivedGoalsUiState(
                    goals = goals.filter { it.isArchived },
                    isLoading = false,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
                initialValue = ArchivedGoalsUiState(),
            )

    fun unarchive(goalId: String) {
        viewModelScope.launch { goalRepository.setArchived(goalId, archived = false) }
    }

    fun delete(goalId: String) {
        viewModelScope.launch { goalRepository.deleteGoal(goalId) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
