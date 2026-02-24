package com.example.todoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.todoapp.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>): List<Long>

    @Query(
        "UPDATE tasks SET is_completed = :isCompleted, updated_at = :updatedAt, sync_status = :syncStatus WHERE id = :taskId"
    )
    suspend fun updateTaskCompletionStatus(
        taskId: String,
        isCompleted: Boolean,
        updatedAt: Long,
        syncStatus: String
    )

    @Query("SELECT * FROM tasks WHERE sync_status NOT IN ('PENDING_DELETE', 'FAILED_DELETE') ORDER BY created_at DESC")
    fun getAllTasksFlow(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = :isCompleted AND sync_status NOT IN ('PENDING_DELETE', 'FAILED_DELETE')")
    fun getTasksByCompletion(isCompleted: Boolean): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' AND sync_status NOT IN ('PENDING_DELETE', 'FAILED_DELETE')")
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE sync_status = 'CONFLICT'")
    fun getConflictedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE sync_status IN (:statuses)")
    suspend fun getTasksBySyncStatuses(statuses: List<String>): List<TaskEntity>

    @Query("UPDATE tasks SET sync_status = :syncStatus, updated_at = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskSyncStatus(taskId: String, syncStatus: String, updatedAt: Long)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: String)

    @Query("DELETE FROM tasks")
    suspend fun clearAllTasks()

    @Transaction
    suspend fun markAllAsCompleted() {
        getIncompleteTasks().forEach { task ->
            updateTaskCompletionStatus(
                taskId = task.id,
                isCompleted = true,
                updatedAt = System.currentTimeMillis(),
                syncStatus = "PENDING"
            )
        }
    }

    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND sync_status NOT IN ('PENDING_DELETE', 'FAILED_DELETE')")
    suspend fun getIncompleteTasks(): List<TaskEntity>
}
