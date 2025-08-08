package com.example.todoapp.domain.usecase.task

import app.cash.turbine.test
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.domain.use_case.task.GetTaskUseCase
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK // O @MockK si prefieres ser más explícito
import io.mockk.junit4.MockKRule // Para inicializar mocks con @MockK o @RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

class GetTaskUseCaseTest {

    @RelaxedMockK
    private lateinit var mockRepository: TaskRepository

    // Regla para inicializar automáticamente los mocks anotados con @MockK o @RelaxedMockK
    @get:Rule
    val mockkRule = MockKRule(this)

    private lateinit var getTaskUseCase: GetTaskUseCase

    @Before
    fun setUp(){
        getTaskUseCase = GetTaskUseCase(mockRepository)
    }

    // ---Test---
    @Test
    fun `invoke cuando repositorio tiene tareas deberia retornar flow con esas tareas`() = runTest {

        // Arrange
        val fakeTasks = listOf(
            Task("1", "Task 1", "Desc 1", false, Instant.now(), 0, Instant.now(), 0,
                LocalTime.NOON, false),
            Task("2", "Task 2", "Desc 2", true, Instant.now(), 1, Instant.now(), 1,
                LocalTime.now(), true)
        )

        // Usamos 'every' porque getLocalTasks() no es suspend. Si lo fuera, usaríamos 'coEvery'.
        every { mockRepository.getLocalTasks() } returns flowOf(fakeTasks)

        //Act
        val resultFlow = getTaskUseCase()

        //Assert
        resultFlow.test {
            // Verifica que el primer (y único) item emitido por el Flow sea la lista fakeTasks
            assertEquals(fakeTasks, awaitItem())
            // Verifica que el Flow complete después de emitir el item
            awaitComplete()
        }

        // Verifica que la función del repositorio fue llamada exactamente una vez
        verify(exactly = 1) { mockRepository.getLocalTasks() }
    }

    @Test
    fun `invoke cuando repositorio no tiene tareas deberia retornar flow con lista vacia`() = runTest {
        // Arrange
        val emptyTasks = emptyList<Task>()
        every { mockRepository.getLocalTasks() } returns flowOf(emptyTasks)

        // Act
        val resultFlow = getTaskUseCase()

        // Assert
        resultFlow.test {
            val emittedItem = awaitItem()
            assertTrue("La lista emitida debería estar vacía", emittedItem.isEmpty())
            awaitComplete()
        }

        verify(exactly = 1) { mockRepository.getLocalTasks() }
    }

    @Test
    fun `invoke cuando repositorio emite error deberia propagar el error`() = runTest {
        // Arrange
        val expectedException = RuntimeException("Database error")
        every { mockRepository.getLocalTasks() } returns flow { throw expectedException }

        // Act
        val resultFlow = getTaskUseCase()

        // Assert
        resultFlow.test {
            // Verifica que el Flow emite el error esperado
            val actualException = awaitError()
            assertEquals(expectedException.message, actualException.message)
            // No se espera awaitComplete() aquí porque el Flow terminó con un error.
        }

        verify(exactly = 1) { mockRepository.getLocalTasks() }
    }


}