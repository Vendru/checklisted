package com.checklisted.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query(
        """
        SELECT * FROM goals
        WHERE :includeArchived = 1 OR isArchived = 0
        ORDER BY position ASC, createdAt ASC
        """,
    )
    fun observeGoals(includeArchived: Boolean): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun observeGoal(id: String): Flow<GoalEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)

    @Query("UPDATE goals SET isArchived = :archived WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM goals")
    suspend fun nextPosition(): Int

    @Query("UPDATE goals SET position = :position WHERE id = :id")
    suspend fun setPosition(id: String, position: Int)

    /** Applied in one transaction so a drag never leaves the list half-reordered. */
    @Transaction
    suspend fun reorder(orderedIds: List<String>) {
        orderedIds.forEachIndexed { index, id -> setPosition(id, index) }
    }
}

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completions WHERE periodKey IN (:periodKeys)")
    fun observeForPeriods(periodKeys: Collection<String>): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions WHERE goalId = :goalId ORDER BY completedAt ASC")
    fun observeForGoal(goalId: String): Flow<List<CompletionEntity>>

    @Query("SELECT * FROM completions")
    fun observeAll(): Flow<List<CompletionEntity>>

    /**
     * IGNORE rather than REPLACE: the unique index already guarantees one row per
     * period, and replacing would rewrite `completedAt`, silently moving when the
     * user says they did the thing.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(completion: CompletionEntity)

    @Query("DELETE FROM completions WHERE goalId = :goalId AND periodKey = :periodKey")
    suspend fun delete(goalId: String, periodKey: String)
}
