package com.example.todoapp.domain.repository

import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskWriteResult
import kotlinx.coroutines.flow.Flow


interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun upsertTask(task: Task): TaskWriteResult
    suspend fun deleteTask(id: String): TaskWriteResult
    suspend fun updateTaskCompletion(taskId: String, isCompleted: Boolean): TaskWriteResult
    suspend fun clearLocalTasks()
}
