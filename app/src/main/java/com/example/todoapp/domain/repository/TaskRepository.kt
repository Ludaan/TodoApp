package com.example.todoapp.domain.repository

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.Task
import kotlinx.coroutines.flow.Flow


interface TaskRepository {
    fun getLocalTasks(): Flow<List<Task>>
    fun getRemoteTasks(): Flow<DataState<List<Task>>>

    suspend fun addLocalTask(task: Task)
    suspend fun addRemoteTask(task: Task)

    suspend fun updateLocalTasks(tasks: List<Task>)
    suspend fun updateRemoteTasks(tasks: List<Task>)

    suspend fun deleteLocalTask(id: String)
    suspend fun deleteRemoteTask(id: String)
}