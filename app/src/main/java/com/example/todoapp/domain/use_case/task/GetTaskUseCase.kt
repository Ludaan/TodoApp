package com.example.todoapp.domain.use_case.task

import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTaskUseCase @Inject constructor(private val repository: TaskRepository) {

    operator fun invoke(): Flow<List<Task>> {
        return repository.getLocalTasks()
    }
}