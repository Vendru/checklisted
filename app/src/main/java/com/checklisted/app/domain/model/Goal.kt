package com.checklisted.app.domain.model

import java.time.Instant

/** A recurring goal the user checks off. */
data class Goal(
    val id: String,
    val title: String,
    val description: String? = null,
    val recurrence: Recurrence,
    val colorTag: String,
    val position: Int,
    val isArchived: Boolean = false,
    val createdAt: Instant,
)

/**
 * A goal ticked off for one period.
 *
 * Toggling a goal inserts or removes one of these. Nothing else ever writes to the
 * history, and a period rollover writes nothing at all.
 */
data class Completion(
    val id: String,
    val goalId: String,
    val periodKey: PeriodKey,
    val completedAt: Instant,
)

/** A goal paired with whether it is done for the period currently on screen. */
data class GoalStatus(
    val goal: Goal,
    val periodKey: PeriodKey,
    val isCompleted: Boolean,
)
