package com.example.todoapp.data.local

import androidx.room.migration.Migration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.local.converter.TaskSyncStatusConverter
import com.example.todoapp.data.local.entities.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(TaskSyncStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE tasks SET updated_at = created_at WHERE updated_at = 0")
            }
        }
    }
}
