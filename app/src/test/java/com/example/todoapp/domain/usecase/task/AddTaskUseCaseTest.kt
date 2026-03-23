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
        coEvery { mockRepository.upsertTask(any()) } returns TaskWriteResult.Synced()

        val result = addTaskUseCase(validNewTask)

        assertTrue(result is TaskWriteResult.Synced)
        coVerify(exactly = 1) { mockRepository.upsertTask(validNewTask) }
    }

    @Test
    fun `invoke cuando falla remoto devuelve PendingSync y marca FAILED`() = runTest {
        coEvery { mockRepository.upsertTask(any()) } returns TaskWriteResult.PendingSync(message = "Error red")

        val result = addTaskUseCase(validNewTask)

        assertTrue(result is TaskWriteResult.PendingSync)
        coVerify(exactly = 1) { mockRepository.upsertTask(validNewTask) }
    }
}
