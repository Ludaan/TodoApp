package com.example.todoapp.domain.usecase.auth

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.SignOutUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SignOutUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockAuthRepository: FirebaseAuthApi
    private lateinit var signOutUseCase: SignOutUseCase

    @Before
    fun setUp(){
        signOutUseCase = SignOutUseCase(mockAuthRepository)
    }

    @Test
    fun `invoke cuando el repositorio cierra sesion exitosamente, deberia devolver Success`() =
        runTest {
            //Arrange
            coEvery { mockAuthRepository.signOut() } returns DataState.Success(Unit)

            //Act
            val result = signOutUseCase()

            //Assert
            assertTrue(result is DataState.Success)
            coVerify(exactly = 1) { mockAuthRepository.signOut() }
        }

    @Test
    fun `Invoke cuando el repositorio falla al cerrar sesion, deberia devolver Error`() =
        runTest {

            val errorMessage = "Error al cerrar sesión"
            //Arrange
            coEvery { mockAuthRepository.signOut() } returns DataState.Error(errorMessage)

            //Act
            val result = signOutUseCase()

            //assert

            assertTrue(result is DataState.Error)
            assertEquals(errorMessage, (result as DataState.Error).message)
            coVerify(exactly = 1) { mockAuthRepository.signOut() }

        }
}