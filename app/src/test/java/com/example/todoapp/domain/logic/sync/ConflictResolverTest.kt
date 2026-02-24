package com.example.todoapp.domain.logic.sync

import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskSyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

class ConflictResolverTest {

    private val resolver = ConflictResolver()

    @Test
    fun `resolve should keep task with newest updatedAt`() {
        val id = "task-1"
        val local = listOf(task(id = id, updatedAt = Instant.parse("2025-01-01T10:00:00Z"), title = "local"))
        val remote = listOf(task(id = id, updatedAt = Instant.parse("2025-01-01T11:00:00Z"), title = "remote"))

        val resolved = resolver.resolve(local, remote)

        assertEquals(1, resolved.size)
        assertEquals("remote", resolved.first().title)
    }

    @Test
    fun `resolve should keep pending delete when timestamps tie`() {
        val id = "task-2"
        val updatedAt = Instant.parse("2025-01-01T11:00:00Z")
        val local = listOf(
            task(
                id = id,
                updatedAt = updatedAt,
                title = "local-delete",
                syncStatus = TaskSyncStatus.PENDING_DELETE
            )
        )
        val remote = listOf(task(id = id, updatedAt = updatedAt, title = "remote"))

        val resolved = resolver.resolve(local, remote)

        assertEquals(1, resolved.size)
        assertEquals(TaskSyncStatus.PENDING_DELETE, resolved.first().syncStatus)
    }

    private fun task(
        id: String,
        updatedAt: Instant,
        title: String,
        syncStatus: TaskSyncStatus = TaskSyncStatus.SYNCED
    ): Task {
        return Task(
            id = id,
            title = title,
            description = "desc",
            isCompleted = false,
            createdAt = Instant.parse("2025-01-01T09:00:00Z"),
            updatedAt = updatedAt,
            color = 0,
            limitDate = Instant.parse("2025-01-10T00:00:00Z"),
            type = 0,
            repeatAt = LocalTime.of(9, 0),
            repeatDaily = false,
            syncStatus = syncStatus
        )
    }
}
