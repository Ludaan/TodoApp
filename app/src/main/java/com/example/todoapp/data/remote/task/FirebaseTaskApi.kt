package com.example.todoapp.data.remote.task

import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.remote.model.RemoteTaskDto
import kotlinx.coroutines.flow.Flow

interface FirebaseTaskApi {
    fun getTasks(): Flow<DataState<List<RemoteTaskDto>>>
    suspend fun addOrUpdateTask(task: RemoteTaskDto)
    suspend fun deleteTask(id: String)
    suspend fun updateTaskCompletionStatus(id: String, isCompleted: Boolean)
}