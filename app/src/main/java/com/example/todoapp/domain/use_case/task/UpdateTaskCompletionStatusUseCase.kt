package com.example.todoapp.domain.use_case.task

import com.example.todoapp.domain.model.TaskWriteResult
import com.example.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskCompletionStatusUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, isCompleted: Boolean): TaskWriteResult {
        return taskRepository.updateTaskCompletion(taskId, isCompleted)
    }
}
