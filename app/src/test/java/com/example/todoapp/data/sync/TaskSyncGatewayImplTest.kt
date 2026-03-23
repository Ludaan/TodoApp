package com.example.todoapp.data.sync

import app.cash.turbine.test
import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.local.entities.TaskEntity
import com.example.todoapp.data.mapper.TaskMapper
import com.example.todoapp.data.remote.model.RemoteTaskDto
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskSyncStatus
import com.example.todoapp.domain.repository.FirebaseTaskApi
import com.google.firebase.Timestamp
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

class TaskSyncGatewayImplTest {

    @MockK
    private lateinit var mockTaskDao: TaskDao

    @MockK
    private lateinit var mockFirebaseApi: FirebaseTaskApi

    private lateinit var gateway: TaskSyncGatewayImpl

    private val createdAt = Instant.parse("2025-01-01T10:00:00Z")
    private val task = Task(
        id = "task-1",
        title = "Task 1",
        description = "desc",
        isCompleted = false,
        createdAt = createdAt,
        updatedAt = createdAt,
        color = 0,
        limitDate = createdAt.plusSeconds(3_600),
        type = 1,
        repeatAt = LocalTime.NOON,
        repeatDaily = false,
        syncStatus = TaskSyncStatus.SYNCED
    )
    private val entity = TaskEntity(
        id = "task-1",
        title = "Task 1",
        description = "desc",
        isCompleted = false,
        createdAt = createdAt.toEpochMilli(),
        updatedAt = createdAt.toEpochMilli(),
        color = 0,
        limitDate = createdAt.plusSeconds(3_600).toEpochMilli(),
        type = 1,
        repeatAt = "12:00",
        repeatDaily = false,
        syncStatus = TaskSyncStatus.SYNCED
    )
    private val remoteDto = RemoteTaskDto(
        id = "task-1",
        title = "Task 1",
        description = "desc",
        isCompleted = false,
        createdAt = Timestamp(entity.createdAt / 1_000, 0),
        updatedAt = Timestamp(entity.updatedAt / 1_000, 0),
        color = 0,
        limitDate = Timestamp(entity.limitDate / 1_000, 0),
        type = 1,
        repeatAt = "12:00",
        repeatDaily = false,
        syncStatus = TaskSyncStatus.SYNCED.name
    )

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        gateway = TaskSyncGatewayImpl(mockTaskDao, mockFirebaseApi)
        mockkObject(TaskMapper)
        every { TaskMapper.fromRemote(remoteDto) } returns task
        every { TaskMapper.fromLocal(entity) } returns task
        every { TaskMapper.toLocal(any()) } answers { entity.copy(syncStatus = firstArg<Task>().syncStatus) }
        every { TaskMapper.toRemote(any()) } answers {
            val mappedTask = firstArg<Task>()
            remoteDto.copy(syncStatus = mappedTask.syncStatus.name)
        }
    }

    @After
    fun tearDown() {
        unmockkObject(TaskMapper)
    }

    @Test
    fun `observeRemoteTasks maps remote tasks to domain`() = runTest {
        every { mockFirebaseApi.getTasks() } returns flowOf(DataState.Success(listOf(remoteDto)))

        gateway.observeRemoteTasks().test {
            val item = awaitItem()
            assertTrue(item is DataState.Success)
            assertEquals(listOf(task), (item as DataState.Success).data)
            awaitComplete()
        }
    }

    @Test
    fun `syncResolvedTasks syncs active tasks and pending deletes`() = runTest {
        val pendingDeleteEntity = entity.copy(id = "delete-1", syncStatus = TaskSyncStatus.PENDING_DELETE)
        val pendingDeleteTask = task.copy(id = "delete-1", syncStatus = TaskSyncStatus.PENDING_DELETE)

        coEvery { mockFirebaseApi.addOrUpdateTask(any()) } returns Unit
        coEvery { mockTaskDao.updateTaskSyncStatus("task-1", TaskSyncStatus.SYNCED, any()) } returns Unit
        coEvery { mockTaskDao.getTasksBySyncStatuses(any()) } returns listOf(pendingDeleteEntity)
        coEvery { mockFirebaseApi.deleteTask("delete-1") } returns Unit
        coEvery { mockTaskDao.deleteTaskById("delete-1") } returns Unit

        val result = gateway.syncResolvedTasks(listOf(task, pendingDeleteTask))

        assertEquals(1, result.syncedCount)
        assertEquals(1, result.deletedCount)
        assertEquals(0, result.failedCount)
        assertEquals(0, result.deleteFailedCount)
        coVerify(exactly = 1) { mockFirebaseApi.addOrUpdateTask(match { it.id == "task-1" }) }
        coVerify(exactly = 1) { mockFirebaseApi.deleteTask("delete-1") }
    }
}
