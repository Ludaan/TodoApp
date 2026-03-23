package com.example.todoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.todoapp.data.local.entities.TaskEntity
import com.example.todoapp.domain.model.TaskSyncStatus
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
        syncStatus: TaskSyncStatus
    )

    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    suspend fun getAllTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE sync_status NOT IN (:excludedStatuses) ORDER BY created_at DESC")
    fun getAllTasksFlow(excludedStatuses: List<TaskSyncStatus>): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE is_completed = :isCompleted AND sync_status NOT IN (:excludedStatuses)")
    fun getTasksByCompletion(
        isCompleted: Boolean,
        excludedStatuses: List<TaskSyncStatus>
    ): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%' AND sync_status NOT IN (:excludedStatuses)")
    fun searchTasks(
        query: String,
        excludedStatuses: List<TaskSyncStatus>
    ): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE sync_status = :status")
    fun getTasksBySyncStatus(status: TaskSyncStatus): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: String): TaskEntity?

    @Query("SELECT * FROM tasks WHERE sync_status IN (:statuses)")
    suspend fun getTasksBySyncStatuses(statuses: List<TaskSyncStatus>): List<TaskEntity>

    @Query("UPDATE tasks SET sync_status = :syncStatus, updated_at = :updatedAt WHERE id = :taskId")
    suspend fun updateTaskSyncStatus(taskId: String, syncStatus: TaskSyncStatus, updatedAt: Long)

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
                syncStatus = TaskSyncStatus.PENDING
            )
        }
    }

    @Query("SELECT * FROM tasks WHERE is_completed = 0 AND sync_status NOT IN (:excludedStatuses)")
    suspend fun getIncompleteTasks(
        excludedStatuses: List<TaskSyncStatus> = listOf(
            TaskSyncStatus.PENDING_DELETE,
            TaskSyncStatus.FAILED_DELETE
        )
    ): List<TaskEntity>
}
