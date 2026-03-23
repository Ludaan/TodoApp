package com.example.todoapp.domain.repository

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.SyncResult
import com.example.todoapp.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskSyncGateway {
    fun observeRemoteTasks(): Flow<DataState<List<Task>>>
    suspend fun getLocalTasksSnapshot(): List<Task>
    suspend fun replaceLocalTasks(tasks: List<Task>)
    suspend fun syncResolvedTasks(tasks: List<Task>): SyncResult
}
