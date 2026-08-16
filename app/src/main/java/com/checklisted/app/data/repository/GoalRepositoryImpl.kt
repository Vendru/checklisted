package com.checklisted.app.data.repository

import com.checklisted.app.data.local.CompletionDao
import com.checklisted.app.data.local.CompletionEntity
import com.checklisted.app.data.local.GoalDao
import com.checklisted.app.data.local.GoalEntity
import com.checklisted.app.domain.model.Completion
import com.checklisted.app.domain.model.Goal
import com.checklisted.app.domain.model.PeriodKey
import com.checklisted.app.domain.model.Recurrence
import com.checklisted.app.domain.repository.CompletionRepository
import com.checklisted.app.domain.repository.GoalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val ioDispatcher: CoroutineDispatcher,
) : GoalRepository {
    override fun observeGoals(includeArchived: Boolean): Flow<List<Goal>> =
        goalDao.observeGoals(includeArchived).map { goals -> goals.map(GoalEntity::toDomain) }

    override fun observeGoal(id: String): Flow<Goal?> =
        goalDao.observeGoal(id).map { it?.toDomain() }

    override suspend fun createGoal(
        title: String,
        description: String?,
        recurrence: Recurrence,
        colorTag: String,
    ): String = withContext(ioDispatcher) {
        val id = UUID.randomUUID().toString()
        goalDao.insert(
            GoalEntity(
                id = id,
                title = title.trim(),
                description = description?.trim()?.takeIf { it.isNotEmpty() },
                recurrence = recurrence.name,
                colorTag = colorTag,
                position = goalDao.nextPosition(),
                isArchived = false,
                createdAt = Instant.now().toEpochMilli(),
            ),
        )
        id
    }

    override suspend fun updateGoal(goal: Goal) = withContext(ioDispatcher) {
        goalDao.update(goal.toEntity())
    }

    override suspend fun setArchived(id: String, archived: Boolean) = withContext(ioDispatcher) {
        goalDao.setArchived(id, archived)
    }

    override suspend fun deleteGoal(id: String) = withContext(ioDispatcher) {
        goalDao.delete(id)
    }

    override suspend fun reorderGoals(orderedIds: List<String>) = withContext(ioDispatcher) {
        goalDao.reorder(orderedIds)
    }
}

@Singleton
class CompletionRepositoryImpl @Inject constructor(
    private val completionDao: CompletionDao,
    private val ioDispatcher: CoroutineDispatcher,
) : CompletionRepository {
    override fun observeCompletions(periodKeys: Collection<PeriodKey>): Flow<List<Completion>> {
        // `IN ()` is not valid SQL, and an empty key set legitimately happens before
        // the first period key is resolved.
        if (periodKeys.isEmpty()) return flowOf(emptyList())
        return completionDao
            .observeForPeriods(periodKeys.map(PeriodKey::value))
            .map { rows -> rows.map(CompletionEntity::toDomain) }
    }

    override fun observeCompletionsForGoal(goalId: String): Flow<List<Completion>> =
        completionDao.observeForGoal(goalId).map { rows -> rows.map(CompletionEntity::toDomain) }

    override fun observeAllCompletions(): Flow<List<Completion>> =
        completionDao.observeAll().map { rows -> rows.map(CompletionEntity::toDomain) }

    override suspend fun setCompleted(
        goalId: String,
        periodKey: PeriodKey,
        completed: Boolean,
        at: Instant,
    ) = withContext(ioDispatcher) {
        if (completed) {
            completionDao.insert(
                CompletionEntity(
                    id = UUID.randomUUID().toString(),
                    goalId = goalId,
                    periodKey = periodKey.value,
                    completedAt = at.toEpochMilli(),
                ),
            )
        } else {
            completionDao.delete(goalId, periodKey.value)
        }
    }
}

private fun GoalEntity.toDomain() = Goal(
    id = id,
    title = title,
    description = description,
    recurrence = runCatching { Recurrence.valueOf(recurrence) }.getOrDefault(Recurrence.DAILY),
    colorTag = colorTag,
    position = position,
    isArchived = isArchived,
    createdAt = Instant.ofEpochMilli(createdAt),
)

private fun Goal.toEntity() = GoalEntity(
    id = id,
    title = title,
    description = description,
    recurrence = recurrence.name,
    colorTag = colorTag,
    position = position,
    isArchived = isArchived,
    createdAt = createdAt.toEpochMilli(),
)

private fun CompletionEntity.toDomain() = Completion(
    id = id,
    goalId = goalId,
    periodKey = PeriodKey(periodKey),
    completedAt = Instant.ofEpochMilli(completedAt),
)
