package com.example.todoapp.domain.use_case.task

import com.example.todoapp.domain.model.TaskWriteResult
import com.example.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(private val repository: TaskRepository) {

    suspend operator fun invoke(taskId: String): TaskWriteResult {
        val now = System.currentTimeMillis()
        repository.markPendingDelete(taskId, now)

        return try {
            repository.deleteRemoteTask(taskId)
            repository.deleteLocalTaskHard(taskId)
            TaskWriteResult.Synced
        } catch (e: Exception) {
            repository.markDeleteFailed(taskId, System.currentTimeMillis())
            TaskWriteResult.PendingSync(e.localizedMessage)
        }
    }
}
