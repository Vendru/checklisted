package com.checklisted.app.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    /** [com.checklisted.app.domain.model.Recurrence] name. */
    val recurrence: String,
    /** [com.checklisted.app.ui.theme.NeoAccent] name. */
    val colorTag: String,
    val position: Int,
    val isArchived: Boolean,
    val createdAt: Long,
)

/**
 * One tick of a goal for one period.
 *
 * The unique index on `(goalId, periodKey)` is what makes completion idempotent —
 * a double tap, or a replayed toggle, cannot produce two rows for the same period.
 *
 * `onDelete = CASCADE` means deleting a goal takes its history with it. That is the
 * destructive path, which the UI confirms; archiving is the way to retire a goal
 * without touching its history.
 */
@Entity(
    tableName = "completions",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["goalId", "periodKey"], unique = true),
        Index(value = ["periodKey"]),
    ],
)
data class CompletionEntity(
    @PrimaryKey val id: String,
    val goalId: String,
    val periodKey: String,
    val completedAt: Long,
)
