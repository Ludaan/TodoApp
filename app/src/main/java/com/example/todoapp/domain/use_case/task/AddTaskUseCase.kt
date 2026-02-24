package com.example.todoapp.domain.use_case.task

import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskSyncStatus
import com.example.todoapp.domain.model.TaskWriteResult
import com.example.todoapp.domain.repository.TaskRepository
import java.time.Instant
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(private val repository: TaskRepository) {

    suspend operator fun invoke(task: Task): TaskWriteResult {
        val now = Instant.now()
        val pendingTask = task.copy(updatedAt = now, syncStatus = TaskSyncStatus.PENDING)
        repository.addLocalTask(pendingTask)

        return try {
            repository.addRemoteTask(pendingTask.copy(syncStatus = TaskSyncStatus.SYNCED))
            repository.ackSynced(task.id, System.currentTimeMillis())
            TaskWriteResult.Synced
        } catch (e: Exception) {
            repository.markFailed(task.id, System.currentTimeMillis())
            TaskWriteResult.PendingSync(e.localizedMessage)
        }
    }
}
