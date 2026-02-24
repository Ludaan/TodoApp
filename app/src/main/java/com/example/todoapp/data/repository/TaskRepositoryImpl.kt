package com.example.todoapp.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.mapper.TaskMapper
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.repository.FirebaseTaskApi
import com.example.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val firebaseApi: FirebaseTaskApi
) : TaskRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getLocalTasks(): Flow<List<Task>> {
        return taskDao.getAllTasksFlow().map { list ->
            list.map { TaskMapper.fromLocal(it) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getLocalTasksSnapshot(): List<Task> {
        return taskDao.getAllTasksFlow().first().map { TaskMapper.fromLocal(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getRemoteTasks(): Flow<DataState<List<Task>>> {
        return firebaseApi.getTasks().map { state ->
            when (state) {
                is DataState.Loading -> DataState.Loading
                is DataState.Success -> DataState.Success(state.data.map { TaskMapper.fromRemote(it) })
                is DataState.Error -> DataState.Error(state.message)
                DataState.Idle -> DataState.Idle
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun addLocalTask(task: Task) {
        taskDao.insertTask(TaskMapper.toLocal(task))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun addRemoteTask(task: Task) {
        firebaseApi.addOrUpdateTask(TaskMapper.toRemote(task))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateLocalTasks(tasks: List<Task>) {
        taskDao.insertTasks(tasks.map { TaskMapper.toLocal(it) })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateRemoteTasks(tasks: List<Task>) {
        tasks.forEach { firebaseApi.addOrUpdateTask(TaskMapper.toRemote(it)) }
    }

    override suspend fun deleteLocalTask(id: String) {
        taskDao.updateTaskSyncStatus(
            taskId = id,
            syncStatus = "PENDING_DELETE",
            updatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun deleteLocalTaskHard(id: String) {
        taskDao.deleteTaskById(id)
    }

    override suspend fun deleteRemoteTask(id: String) {
        firebaseApi.deleteTask(id)
    }

    override suspend fun updateLocalTaskCompletionStatus(
        taskId: String,
        isCompleted: Boolean,
        updatedAtEpochMillis: Long
    ) {
        taskDao.updateTaskCompletionStatus(
            taskId = taskId,
            isCompleted = isCompleted,
            updatedAt = updatedAtEpochMillis,
            syncStatus = "PENDING"
        )
    }

    override suspend fun updateRemoteTaskCompletionStatus(
        taskId: String,
        isCompleted: Boolean,
        updatedAtEpochMillis: Long
    ) {
        firebaseApi.updateTaskCompletionStatus(taskId, isCompleted, updatedAtEpochMillis)
    }

    override suspend fun markPending(taskId: String, updatedAtEpochMillis: Long) {
        taskDao.updateTaskSyncStatus(taskId, "PENDING", updatedAtEpochMillis)
    }

    override suspend fun markFailed(taskId: String, updatedAtEpochMillis: Long) {
        taskDao.updateTaskSyncStatus(taskId, "FAILED", updatedAtEpochMillis)
    }

    override suspend fun ackSynced(taskId: String, updatedAtEpochMillis: Long) {
        taskDao.updateTaskSyncStatus(taskId, "SYNCED", updatedAtEpochMillis)
    }

    override suspend fun markPendingDelete(taskId: String, updatedAtEpochMillis: Long) {
        taskDao.updateTaskSyncStatus(taskId, "PENDING_DELETE", updatedAtEpochMillis)
    }

    override suspend fun markDeleteFailed(taskId: String, updatedAtEpochMillis: Long) {
        taskDao.updateTaskSyncStatus(taskId, "FAILED_DELETE", updatedAtEpochMillis)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getPendingTasks(): List<Task> {
        return taskDao.getTasksBySyncStatuses(listOf("PENDING", "FAILED"))
            .map { TaskMapper.fromLocal(it) }
    }

    override suspend fun getPendingDeleteTaskIds(): List<String> {
        return taskDao.getTasksBySyncStatuses(listOf("PENDING_DELETE", "FAILED_DELETE"))
            .map { it.id }
    }

    override suspend fun clearLocalTasks() {
        taskDao.clearAllTasks()
    }
}
