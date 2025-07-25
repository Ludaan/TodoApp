package com.example.todoapp.domain.usecase

import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.domain.use_case.task.AddTaskUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.util.UUID // Para generar IDs únicos si es necesario

class AddTaskUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockRepository: TaskRepository

    private lateinit var addTaskUseCase: AddTaskUseCase

    // Tarea de ejemplo bien formada
    private val validNewTask = Task(
        UUID.randomUUID().toString(), // Asumimos que el ID se genera antes de llamar al UseCase
        title = "Nueva Tarea de Prueba",
        description = "Descripción de la nueva tarea",
        isCompleted = false,
        createdAt = Instant.now(), // Asumimos que createdAt se establece antes
        color = 0,
        limitDate = Instant.now().plusSeconds(86400 * 2), // Dos días en el futuro
        type = 1,
        repeatAt = LocalTime.NOON,
        repeatDaily = false
    )

    @Before
    fun setUp() {
        addTaskUseCase = AddTaskUseCase(mockRepository)
    }

    @Test
    fun `invoke con tarea valida deberia llamar a addLocalTask y addRemoteTask en el repositorio`() = runTest {
        // Arrange
        coEvery { mockRepository.addLocalTask(any()) } just Runs
        coEvery { mockRepository.addRemoteTask(any()) } just Runs

        // Act
        addTaskUseCase(validNewTask)

        // Assert
        coVerify(exactly = 1) { mockRepository.addLocalTask(validNewTask) }
        coVerify(exactly = 1) { mockRepository.addRemoteTask(validNewTask) }
    }

    @Test
    fun `invoke cuando addLocalTask lanza excepcion deberia propagarla y no llamar a addRemoteTask`() = runTest {
        // Arrange
        val expectedException = RuntimeException("Error añadiendo localmente")
        coEvery { mockRepository.addLocalTask(validNewTask) } throws expectedException

        var actualException: Exception? = null
        try {
            // Act
            addTaskUseCase(validNewTask)
            fail("Se esperaba una excepción pero no fue lanzada")
        } catch (e: Exception) {
            actualException = e
        }

        // Assert
        assertEquals("La excepción lanzada no es la esperada", expectedException, actualException)
        coVerify(exactly = 1) { mockRepository.addLocalTask(validNewTask) }
        coVerify(exactly = 0) { mockRepository.addRemoteTask(any()) } // No debería llamarse
    }

    @Test
    fun `invoke cuando addRemoteTask lanza excepcion (despues de exito en local) deberia propagarla`() = runTest {
        // Arrange
        val expectedException = RuntimeException("Error añadiendo remotamente")
        coEvery { mockRepository.addLocalTask(validNewTask) } just Runs // Local tiene éxito
        coEvery { mockRepository.addRemoteTask(validNewTask) } throws expectedException // Remoto falla

        var actualException: Exception? = null
        try {
            // Act
            addTaskUseCase(validNewTask)
            fail("Se esperaba una excepción pero no fue lanzada")
        } catch (e: Exception) {
            actualException = e
        }

        // Assert
        assertEquals("La excepción lanzada no es la esperada", expectedException, actualException)
        coVerify(exactly = 1) { mockRepository.addLocalTask(validNewTask) }
        coVerify(exactly = 1) { mockRepository.addRemoteTask(validNewTask) }
    }

}