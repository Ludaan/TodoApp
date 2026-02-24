package com.example.todoapp.domain.use_case.task

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskSyncStatus
import com.example.todoapp.domain.model.TaskWriteResult
import com.example.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

class AddTaskUseCaseTest {

    @Test
    fun `invoke returns pending sync when remote fails and marks task as failed`() = runBlocking {
        val repo = FakeTaskRepository(remoteShouldFail = true)
        val useCase = AddTaskUseCase(repo)
        val task = sampleTask()

        val result = useCase(task)

        assertTrue(result is TaskWriteResult.PendingSync)
        assertEquals("FAILED", repo.statusById[task.id])
    }

    private fun sampleTask() = Task(
        id = "task-1",
        title = "title",
        description = "desc",
        isCompleted = false,
        createdAt = Instant.parse("2025-01-01T09:00:00Z"),
        updatedAt = Instant.parse("2025-01-01T09:00:00Z"),
        color = 0,
        limitDate = Instant.parse("2025-01-10T00:00:00Z"),
        type = 0,
        repeatAt = LocalTime.of(9, 0),
        repeatDaily = false,
        syncStatus = TaskSyncStatus.PENDING
    )

    private class FakeTaskRepository(
        private val remoteShouldFail: Boolean
    ) : TaskRepository {
        val statusById = mutableMapOf<String, String>()
        private val tasks = mutableMapOf<String, Task>()

        override fun getLocalTasks(): Flow<List<Task>> = flowOf(tasks.values.toList())
        override suspend fun getLocalTasksSnapshot(): List<Task> = tasks.values.toList()
        override fun getRemoteTasks(): Flow<DataState<List<Task>>> = flowOf(DataState.Success(emptyList()))

        override suspend fun addLocalTask(task: Task) {
            tasks[task.id] = task
            statusById[task.id] = task.syncStatus.name
        }

        override suspend fun addRemoteTask(task: Task) {
            if (remoteShouldFail) error("network")
            statusById[task.id] = "SYNCED"
        }

        override suspend fun updateLocalTasks(tasks: List<Task>) = Unit
        override suspend fun updateRemoteTasks(tasks: List<Task>) = Unit
        override suspend fun deleteLocalTask(id: String) = Unit
        override suspend fun deleteLocalTaskHard(id: String) = Unit
        override suspend fun deleteRemoteTask(id: String) = Unit
        override suspend fun updateLocalTaskCompletionStatus(
            taskId: String,
            isCompleted: Boolean,
            updatedAtEpochMillis: Long
        ) = Unit

        override suspend fun updateRemoteTaskCompletionStatus(
            taskId: String,
            isCompleted: Boolean,
            updatedAtEpochMillis: Long
        ) = Unit

        override suspend fun markPending(taskId: String, updatedAtEpochMillis: Long) {
            statusById[taskId] = "PENDING"
        }

        override suspend fun markFailed(taskId: String, updatedAtEpochMillis: Long) {
            statusById[taskId] = "FAILED"
        }

        override suspend fun ackSynced(taskId: String, updatedAtEpochMillis: Long) {
            statusById[taskId] = "SYNCED"
        }

        override suspend fun markPendingDelete(taskId: String, updatedAtEpochMillis: Long) = Unit
        override suspend fun markDeleteFailed(taskId: String, updatedAtEpochMillis: Long) = Unit
        override suspend fun getPendingTasks(): List<Task> = emptyList()
        override suspend fun getPendingDeleteTaskIds(): List<String> = emptyList()
        override suspend fun clearLocalTasks() = Unit
    }
}
