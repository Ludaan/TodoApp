package com.example.todoapp.domain.use_case.task

import com.example.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

class ClearLocalTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke() {
        taskRepository.clearLocalTasks()
    }
}
