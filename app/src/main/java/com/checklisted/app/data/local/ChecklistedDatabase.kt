package com.checklisted.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [GoalEntity::class, CompletionEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ChecklistedDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao

    abstract fun completionDao(): CompletionDao

    companion object {
        const val NAME = "checklisted.db"
    }
}
