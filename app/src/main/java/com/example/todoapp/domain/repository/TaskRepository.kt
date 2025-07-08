package com.example.todoapp.domain.repository

import com.example.todoapp.data.local.entities.TaskEntity
import com.example.todoapp.domain.model.Task


interface TaskRepository {
    suspend fun getLocalTasks(): List<Task>
    suspend fun getRemoteTasks(): List<Task>

    suspend fun addLocalTask(task: Task)
    suspend fun addRemoteTask(task: Task)

    suspend fun updateLocalTasks(tasks: List<Task>)
    suspend fun updateRemoteTasks(tasks: List<Task>)

    suspend fun deleteLocalTask(id: String)
    suspend fun deleteRemoteTask(id: String)
}