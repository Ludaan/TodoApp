package com.example.todoapp.data.sync

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.mapper.TaskMapper
import com.example.todoapp.domain.model.SyncResult
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskSyncStatus
import com.example.todoapp.domain.repository.FirebaseTaskApi
import com.example.todoapp.domain.repository.TaskSyncGateway
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskSyncGatewayImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val firebaseApi: FirebaseTaskApi
) : TaskSyncGateway {

    private val deletedStatuses = listOf(
        TaskSyncStatus.PENDING_DELETE,
        TaskSyncStatus.FAILED_DELETE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    override fun observeRemoteTasks(): Flow<DataState<List<Task>>> {
        return firebaseApi.getTasks().map { state ->
            when (state) {
                is DataState.Loading -> DataState.Loading
                is DataState.Success -> DataState.Success(state.data.map(TaskMapper::fromRemote))
                is DataState.Error -> DataState.Error(state.message)
                DataState.Idle -> DataState.Idle
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getLocalTasksSnapshot(): List<Task> {
        return taskDao.getAllTasks().map(TaskMapper::fromLocal)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun replaceLocalTasks(tasks: List<Task>) {
        taskDao.insertTasks(tasks.map(TaskMapper::toLocal))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun syncResolvedTasks(tasks: List<Task>): SyncResult {
        val activeSyncResult = syncActiveTasks(
            tasks.filterNot { it.syncStatus in deletedStatuses }
        )
        val deleteSyncResult = syncPendingDeletes()

        return SyncResult(
            syncedCount = activeSyncResult.successIds.size,
            failedCount = activeSyncResult.failedIds.size,
            deletedCount = deleteSyncResult.successIds.size,
            deleteFailedCount = deleteSyncResult.failedIds.size,
            failedTaskIds = activeSyncResult.failedIds,
            failedDeleteTaskIds = deleteSyncResult.failedIds
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncActiveTasks(tasks: List<Task>): BatchSyncResult {
        val successIds = mutableListOf<String>()
        val failedIds = mutableListOf<String>()

        tasks.forEach { task ->
            try {
                firebaseApi.addOrUpdateTask(TaskMapper.toRemote(task.copy(syncStatus = TaskSyncStatus.SYNCED)))
                updateSyncStatus(task.id, TaskSyncStatus.SYNCED, System.currentTimeMillis())
                successIds += task.id
            } catch (_: Exception) {
                updateSyncStatus(task.id, TaskSyncStatus.FAILED, System.currentTimeMillis())
                failedIds += task.id
            }
        }

        return BatchSyncResult(successIds = successIds, failedIds = failedIds)
    }

    private suspend fun syncPendingDeletes(): BatchSyncResult {
        val successIds = mutableListOf<String>()
        val failedIds = mutableListOf<String>()

        taskDao.getTasksBySyncStatuses(deletedStatuses).forEach { task ->
            try {
                firebaseApi.deleteTask(task.id)
                taskDao.deleteTaskById(task.id)
                successIds += task.id
            } catch (_: Exception) {
                updateSyncStatus(task.id, TaskSyncStatus.FAILED_DELETE, System.currentTimeMillis())
                failedIds += task.id
            }
        }

        return BatchSyncResult(successIds = successIds, failedIds = failedIds)
    }

    private suspend fun updateSyncStatus(
        taskId: String,
        status: TaskSyncStatus,
        updatedAtEpochMillis: Long
    ) {
        taskDao.updateTaskSyncStatus(taskId, status, updatedAtEpochMillis)
    }

    private data class BatchSyncResult(
        val successIds: List<String>,
        val failedIds: List<String>
    )
}
