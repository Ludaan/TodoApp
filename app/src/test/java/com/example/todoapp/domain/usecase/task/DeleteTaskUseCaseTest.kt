package com.example.todoapp.domain.usecase.task

import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.domain.use_case.task.DeleteTaskUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail // Para forzar un fallo si no se lanza la excepción esperada
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeleteTaskUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this) // Inicializa los mocks de MockK

    @RelaxedMockK // Crea un mock del repositorio. Relaxed para no tener que stubbear todo.
    private lateinit var mockRepository: TaskRepository

    private lateinit var deleteTaskUseCase: DeleteTaskUseCase

    private val testTaskId = "test-task-id-123"

    @Before
    fun setUp() {
        // Crea la instancia del caso de uso con el repositorio mockeado ANTES de cada test
        deleteTaskUseCase = DeleteTaskUseCase(mockRepository)
    }

    @Test
    fun `invoke con taskId valido deberia llamar a deleteRemoteTask y deleteLocalTask en el repositorio`() = runTest {
        // Arrange
        // Configura los mocks para que no hagan nada cuando se llamen (son suspend Unit)
        coEvery { mockRepository.deleteRemoteTask(any()) } just runs
        coEvery { mockRepository.deleteLocalTask(any()) } just runs

        // Act
        deleteTaskUseCase(testTaskId) // Llama al caso de uso con el ID de prueba

        // Assert
        // Verifica que deleteRemoteTask fue llamado exactamente una vez con testTaskId
        coVerify(exactly = 1) { mockRepository.deleteRemoteTask(testTaskId) }
        // Verifica que deleteLocalTask fue llamado exactamente una vez con testTaskId
        coVerify(exactly = 1) { mockRepository.deleteLocalTask(testTaskId) }
    }

    @Test
    fun `invoke cuando deleteRemoteTask lanza excepcion deberia propagarla y no llamar a deleteLocalTask`() = runTest {
        // Arrange
        val expectedException = RuntimeException("Error borrando remotamente")
        // Configura deleteRemoteTask para que lance una excepción
        coEvery { mockRepository.deleteRemoteTask(testTaskId) } throws expectedException
        // No necesitamos stubbear deleteLocalTask aquí, ya que no debería ser llamada

        var actualException: Exception? = null
        try {
            // Act
            deleteTaskUseCase(testTaskId)
            fail("Se esperaba una excepción pero no fue lanzada") // Si llega aquí, la prueba falla
        } catch (e: Exception) {
            actualException = e
        }

        // Assert
        assertEquals("La excepción lanzada no es la esperada", expectedException, actualException)
        // Verifica que deleteRemoteTask fue llamado
        coVerify(exactly = 1) { mockRepository.deleteRemoteTask(testTaskId) }
        // Verifica que deleteLocalTask NO fue llamado
        coVerify(exactly = 0) { mockRepository.deleteLocalTask(any()) }
    }

    @Test
    fun `invoke cuando deleteLocalTask lanza excepcion (despues de exito en remote) deberia propagarla`() = runTest {
        // Arrange
        val expectedException = RuntimeException("Error borrando localmente")
        // deleteRemoteTask tiene éxito
        coEvery { mockRepository.deleteRemoteTask(testTaskId) } just runs
        // deleteLocalTask lanza una excepción
        coEvery { mockRepository.deleteLocalTask(testTaskId) } throws expectedException

        var actualException: Exception? = null
        try {
            // Act
            deleteTaskUseCase(testTaskId)
            fail("Se esperaba una excepción pero no fue lanzada")
        } catch (e: Exception) {
            actualException = e
        }

        // Assert
        assertEquals("La excepción lanzada no es la esperada", expectedException, actualException)
        // Verifica que deleteRemoteTask fue llamado (debería haber tenido éxito)
        coVerify(exactly = 1) { mockRepository.deleteRemoteTask(testTaskId) }
        // Verifica que deleteLocalTask fue llamado (y lanzó la excepción)
        coVerify(exactly = 1) { mockRepository.deleteLocalTask(testTaskId) }
    }
}