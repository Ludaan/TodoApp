package com.example.todoapp.domain.use_case.task

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
    fun `invoke returns pending sync when repository reports offline write`() = runBlocking {
        val repo = FakeTaskRepository(
            nextWriteResult = TaskWriteResult.PendingSync(message = "network")
        )
        val useCase = AddTaskUseCase(repo)
        val task = sampleTask()

        val result = useCase(task)

        assertTrue(result is TaskWriteResult.PendingSync)
        assertEquals(task, repo.lastUpsertedTask)
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
        private val nextWriteResult: TaskWriteResult
    ) : TaskRepository {
        var lastUpsertedTask: Task? = null

        override fun observeTasks(): Flow<List<Task>> = flowOf(emptyList())

        override suspend fun upsertTask(task: Task): TaskWriteResult {
            lastUpsertedTask = task
            return nextWriteResult
        }

        override suspend fun deleteTask(id: String): TaskWriteResult = TaskWriteResult.Synced()

        override suspend fun updateTaskCompletion(
            taskId: String,
            isCompleted: Boolean
        ): TaskWriteResult = TaskWriteResult.Synced()

        override suspend fun clearLocalTasks() = Unit
    }
}
