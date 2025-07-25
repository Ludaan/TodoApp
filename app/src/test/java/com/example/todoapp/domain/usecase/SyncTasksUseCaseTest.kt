package com.example.todoapp.domain.usecase

import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import app.cash.turbine.test // Para probar Flows
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.logic.sync.SyncManager
import com.example.todoapp.domain.use_case.sync.SyncTasksUseCase
import org.junit.Assert

class SyncTasksUseCaseTest {
    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockSyncManager: SyncManager

    private lateinit var syncTasksUseCase: SyncTasksUseCase

    @Before
    fun setUp() {
        syncTasksUseCase = SyncTasksUseCase(mockSyncManager)
    }

    @Test
    fun `invoke deberia llamar a syncManager syncReactive y devolver su flow`() = runTest {
        // Arrange
        // Preparamos un Flow<DataState<Unit>> de ejemplo que esperamos que syncReactive() devuelva
        val expectedFlow: Flow<DataState<Unit>> = flowOf(
            DataState.Loading,
            DataState.Success(Unit) // Unit porque T en DataState<T> es Unit
        )

        // Cuando syncManager.syncReactive() sea llamado, devuelve nuestro expectedFlow
        every { mockSyncManager.syncReactive() } returns expectedFlow

        // Act
        val actualFlow = syncTasksUseCase() // Llama al operador invoke

        // Assert
        // 1. Verifica que el Flow devuelto por el UseCase es el mismo que el devuelto por el mock
        assertEquals("El Flow devuelto no es el esperado", expectedFlow, actualFlow)

        // 2. Opcional pero recomendado: Verifica el contenido del Flow usando Turbine
        actualFlow.test {
            assertEquals(DataState.Loading, awaitItem())
            assertEquals(DataState.Success(Unit), awaitItem())
            awaitComplete()
        }

        // 3. Verifica que syncManager.syncReactive() fue llamado exactamente una vez
        verify(exactly = 1) { mockSyncManager.syncReactive() }
    }

    @Test
    fun `invoke cuando syncManager syncReactive emite error deberia propagar el flow con error`() = runTest {
        // Arrange
        val errorMessage = "Error durante la sincronización"
        val errorFlow: Flow<DataState<Unit>> = flowOf(
            DataState.Loading,
            DataState.Error(errorMessage)
        )
        every { mockSyncManager.syncReactive() } returns errorFlow

        // Act
        val resultFlow = syncTasksUseCase()

        // Assert
        assertEquals(errorFlow, resultFlow)

        resultFlow.test {
            assertEquals(DataState.Loading, awaitItem())
            val errorState = awaitItem()
            Assert.assertTrue(errorState is DataState.Error)
            assertEquals(errorMessage, (errorState as DataState.Error).message)
            awaitComplete() // Asumiendo que DataState.Error es un estado terminal válido del flow, no una excepción
        }

        verify(exactly = 1) { mockSyncManager.syncReactive() }
    }
}