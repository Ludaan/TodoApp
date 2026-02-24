package com.example.todoapp.domain.repository

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.Task
import kotlinx.coroutines.flow.Flow


interface TaskRepository {
    fun getLocalTasks(): Flow<List<Task>>
    suspend fun getLocalTasksSnapshot(): List<Task>
    fun getRemoteTasks(): Flow<DataState<List<Task>>>

    suspend fun addLocalTask(task: Task)
    suspend fun addRemoteTask(task: Task)

    suspend fun updateLocalTasks(tasks: List<Task>)
    suspend fun updateRemoteTasks(tasks: List<Task>)

    suspend fun deleteLocalTask(id: String)
    suspend fun deleteLocalTaskHard(id: String)
    suspend fun deleteRemoteTask(id: String)

    suspend fun updateLocalTaskCompletionStatus(taskId: String, isCompleted: Boolean, updatedAtEpochMillis: Long)
    suspend fun updateRemoteTaskCompletionStatus(taskId: String, isCompleted: Boolean, updatedAtEpochMillis: Long)

    suspend fun markPending(taskId: String, updatedAtEpochMillis: Long)
    suspend fun markFailed(taskId: String, updatedAtEpochMillis: Long)
    suspend fun ackSynced(taskId: String, updatedAtEpochMillis: Long)
    suspend fun markPendingDelete(taskId: String, updatedAtEpochMillis: Long)
    suspend fun markDeleteFailed(taskId: String, updatedAtEpochMillis: Long)
    suspend fun getPendingTasks(): List<Task>
    suspend fun getPendingDeleteTaskIds(): List<String>
    suspend fun clearLocalTasks()

}
