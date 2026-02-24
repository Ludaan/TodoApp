package com.example.todoapp.domain.use_case.task

import com.example.todoapp.domain.model.TaskWriteResult
import com.example.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskCompletionStatusUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: String, isCompleted: Boolean): TaskWriteResult {
        val now = System.currentTimeMillis()
        taskRepository.updateLocalTaskCompletionStatus(taskId, isCompleted, now)

        return try {
            taskRepository.updateRemoteTaskCompletionStatus(taskId, isCompleted, now)
            taskRepository.ackSynced(taskId, System.currentTimeMillis())
            TaskWriteResult.Synced
        } catch (e: Exception) {
            taskRepository.markFailed(taskId, System.currentTimeMillis())
            TaskWriteResult.PendingSync(e.localizedMessage)
        }

    }
}
