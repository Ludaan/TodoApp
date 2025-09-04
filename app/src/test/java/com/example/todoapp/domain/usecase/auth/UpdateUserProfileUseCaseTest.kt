package com.example.todoapp.domain.usecase.auth

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.UpdateUserProfileUseCase
import com.example.todoapp.domain.use_case.auth.params.UpdateUserProfileParams
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateUserProfileUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockAuthRepository: FirebaseAuthApi

    private lateinit var updateUserProfileUseCase: UpdateUserProfileUseCase

    @Before
    fun setUp() {
        updateUserProfileUseCase = UpdateUserProfileUseCase(mockAuthRepository)
    }

    @Test
    fun `invoke con displayName y photoUrl nulos, deberia devolver Error sin llamar al repositorio`() = runTest {
        // Arrange
        val params = UpdateUserProfileParams(displayName = null, photoUrl = null)
        val expectedErrorMessage = "At least one field (displayName or photoUrl) must be provided to update."

        // Act
        val result = updateUserProfileUseCase(params)

        // Assert
        assertTrue(result is DataState.Error)
        assertEquals(expectedErrorMessage, (result as DataState.Error).message)
        coVerify(exactly = 0) { mockAuthRepository.updateUserProfile(any(), any()) }
    }

    @Test
    fun `invoke con displayName valido y repositorio exitoso, deberia devolver Success`() = runTest {
        // Arrange
        val params = UpdateUserProfileParams(displayName = "Nuevo Nombre", photoUrl = null)
        coEvery { mockAuthRepository.updateUserProfile(params.displayName, params.photoUrl) } returns DataState.Success(Unit)

        // Act
        val result = updateUserProfileUseCase(params)

        // Assert
        assertTrue(result is DataState.Success)
        coVerify(exactly = 1) { mockAuthRepository.updateUserProfile(params.displayName, params.photoUrl) }
    }

    @Test
    fun `invoke con photoUrl valido y repositorio exitoso, deberia devolver Success`() = runTest {
        // Arrange
        val params = UpdateUserProfileParams(displayName = null, photoUrl = "http://example.com/photo.jpg")
        coEvery { mockAuthRepository.updateUserProfile(params.displayName, params.photoUrl) } returns DataState.Success(Unit)

        // Act
        val result = updateUserProfileUseCase(params)

        // Assert
        assertTrue(result is DataState.Success)
        coVerify(exactly = 1) { mockAuthRepository.updateUserProfile(params.displayName, params.photoUrl) }
    }

    @Test
    fun `invoke con displayName y photoUrl validos y repositorio exitoso, deberia devolver Success`() = runTest {
        // Arrange
        val params = UpdateUserProfileParams(displayName = "Nuevo Nombre", photoUrl = "http://example.com/photo.jpg")
        coEvery { mockAuthRepository.updateUserProfile(params.displayName, params.photoUrl) } returns DataState.Success(Unit)

        // Act
        val result = updateUserProfileUseCase(params)

        // Assert
        assertTrue(result is DataState.Success)
        coVerify(exactly = 1) { mockAuthRepository.updateUserProfile(params.displayName, params.photoUrl) }
    }

    @Test
    fun `invoke con displayName valido y repositorio falla, deberia devolver Error`() = runTest {
        // Arrange
        val params = UpdateUserProfileParams(displayName = "Nuevo Nombre", photoUrl = null)
        val errorMessage = "Error al actualizar perfil"
        coEvery { mockAuthRepository.updateUserProfile(params.displayName, params.photoUrl) } returns DataState.Error(errorMessage)

        // Act
        val result = updateUserProfileUseCase(params)

        // Assert
        assertTrue(result is DataState.Error)
        assertEquals(errorMessage, (result as DataState.Error).message)
        coVerify(exactly = 1) { mockAuthRepository.updateUserProfile(params.displayName, params.photoUrl) }
    }
}