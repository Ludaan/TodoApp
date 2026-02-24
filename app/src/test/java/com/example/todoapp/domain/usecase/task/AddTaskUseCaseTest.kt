package com.example.todoapp.domain.usecase.task

import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskWriteResult
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.domain.use_case.task.AddTaskUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

class AddTaskUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockRepository: TaskRepository

    private lateinit var addTaskUseCase: AddTaskUseCase

    private val validNewTask = Task(
        UUID.randomUUID().toString(),
        title = "Nueva Tarea de Prueba",
        description = "Descripción de la nueva tarea",
        isCompleted = false,
        createdAt = Instant.now(),
        color = 0,
        limitDate = Instant.now().plusSeconds(86400 * 2),
        type = 1,
        repeatAt = LocalTime.NOON,
        repeatDaily = false
    )

    @Before
    fun setUp() {
        addTaskUseCase = AddTaskUseCase(mockRepository)
    }

    @Test
    fun `invoke con red disponible devuelve Synced`() = runTest {
        coEvery { mockRepository.addLocalTask(any()) } just Runs
        coEvery { mockRepository.addRemoteTask(any()) } just Runs
        coEvery { mockRepository.ackSynced(any(), any()) } just Runs

        val result = addTaskUseCase(validNewTask)

        assertTrue(result is TaskWriteResult.Synced)
        coVerify(exactly = 1) { mockRepository.addLocalTask(any()) }
        coVerify(exactly = 1) { mockRepository.addRemoteTask(any()) }
        coVerify(exactly = 1) { mockRepository.ackSynced(validNewTask.id, any()) }
    }

    @Test
    fun `invoke cuando falla remoto devuelve PendingSync y marca FAILED`() = runTest {
        coEvery { mockRepository.addLocalTask(any()) } just Runs
        coEvery { mockRepository.addRemoteTask(any()) } throws RuntimeException("Error red")
        coEvery { mockRepository.markFailed(any(), any()) } just Runs

        val result = addTaskUseCase(validNewTask)

        assertTrue(result is TaskWriteResult.PendingSync)
        coVerify(exactly = 1) { mockRepository.addLocalTask(any()) }
        coVerify(exactly = 1) { mockRepository.addRemoteTask(any()) }
        coVerify(exactly = 1) { mockRepository.markFailed(validNewTask.id, any()) }
    }
}
