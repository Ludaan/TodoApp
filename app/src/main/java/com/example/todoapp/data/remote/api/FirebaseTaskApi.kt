package com.example.todoapp.data.remote.api

import com.example.todoapp.data.remote.model.RemoteTaskDto

interface FirebaseTaskApi {
    suspend fun getTasks(): List<RemoteTaskDto>
    suspend fun addOrUpdateTask(task: RemoteTaskDto)
    suspend fun deleteTask(id: String)
}