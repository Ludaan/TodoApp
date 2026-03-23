package com.example.todoapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.mapper.TaskMapper
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskSyncStatus
import com.example.todoapp.domain.model.TaskWriteResult
import com.example.todoapp.domain.repository.FirebaseTaskApi
import com.example.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val firebaseApi: FirebaseTaskApi
) : TaskRepository {

    private val deletedStatuses = listOf(
        TaskSyncStatus.PENDING_DELETE,
        TaskSyncStatus.FAILED_DELETE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun observeTasks(): Flow<List<Task>> {
        return taskDao.getAllTasksFlow(deletedStatuses).map { list ->
            list.map(TaskMapper::fromLocal)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun upsertTask(task: Task): TaskWriteResult {
        val now = Instant.now()
        val pendingTask = task.copy(
            updatedAt = now,
            syncStatus = TaskSyncStatus.PENDING
        )
        taskDao.insertTask(TaskMapper.toLocal(pendingTask))

        return try {
            firebaseApi.addOrUpdateTask(TaskMapper.toRemote(pendingTask.copy(syncStatus = TaskSyncStatus.SYNCED)))
            updateSyncStatus(task.id, TaskSyncStatus.SYNCED, System.currentTimeMillis())
            TaskWriteResult.Synced()
        } catch (e: Exception) {
            updateSyncStatus(task.id, TaskSyncStatus.FAILED, System.currentTimeMillis())
            TaskWriteResult.PendingSync(message = e.localizedMessage)
        }
    }

    override suspend fun deleteTask(id: String): TaskWriteResult {
        updateSyncStatus(id, TaskSyncStatus.PENDING_DELETE, System.currentTimeMillis())

        return try {
            firebaseApi.deleteTask(id)
            taskDao.deleteTaskById(id)
            TaskWriteResult.Synced()
        } catch (e: Exception) {
            updateSyncStatus(id, TaskSyncStatus.FAILED_DELETE, System.currentTimeMillis())
            TaskWriteResult.PendingSync(message = e.localizedMessage)
        }
    }

    override suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean): TaskWriteResult {
        val now = System.currentTimeMillis()
        taskDao.updateTaskCompletionStatus(
            taskId = taskId,
            isCompleted = isCompleted,
            updatedAt = now,
            syncStatus = TaskSyncStatus.PENDING
        )

        return try {
            firebaseApi.updateTaskCompletionStatus(taskId, isCompleted, now)
            updateSyncStatus(taskId, TaskSyncStatus.SYNCED, System.currentTimeMillis())
            TaskWriteResult.Synced()
        } catch (e: Exception) {
            updateSyncStatus(taskId, TaskSyncStatus.FAILED, System.currentTimeMillis())
            TaskWriteResult.PendingSync(message = e.localizedMessage)
        }
    }

    override suspend fun clearLocalTasks() {
        taskDao.clearAllTasks()
    }

    private suspend fun updateSyncStatus(
        taskId: String,
        status: TaskSyncStatus,
        updatedAtEpochMillis: Long
    ) {
        taskDao.updateTaskSyncStatus(taskId, status, updatedAtEpochMillis)
    }
}
