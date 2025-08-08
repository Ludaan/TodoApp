package com.example.todoapp.domain.usecase.auth

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.User
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.RegisterUserUseCase
import com.example.todoapp.domain.use_case.auth.params.RegisterUserParams
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RegisterUserUseCaseTest {

    @get:Rule
    val mockKRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockRepository: FirebaseAuthApi

    private lateinit var registerUserUseCase: RegisterUserUseCase

    // params de ejemplo

    private val validUser = RegisterUserParams(
        email = "luisdla626@gmail.com",
        username = "daniel",
        password = "1234567789"
    )

    private val mockDomainUser = User(
        email = "luisdla626@gmail.com",
        displayName = "daniel",
        uid = "111",
        photoUrl = null,
        isEmailVerified = false
    )

    @Before
    fun setUp() {
        registerUserUseCase = RegisterUserUseCase(mockRepository)
    }

    @Test
    fun `invoke con usuario valido deberia llamar registerUser en el repositorio y devolver DataState Success`() = runTest {

        coEvery { mockRepository.registerUser(params = validUser) } returns DataState.Success(mockDomainUser)

        val result = registerUserUseCase(validUser)

        coVerify(exactly = 1) { mockRepository.registerUser(validUser) }

        assertTrue("El resultado debería ser DataState.Success", result is DataState.Success)
        assertEquals(mockDomainUser, (result as DataState.Success).data)

    }

    @Test
    fun `invoke cuando el repositorio devuelve error deberia devolver DataState Error`() = runTest {
        // Arrange
        val errorMessage = "Error en el registro desde el repositorio"
        coEvery { mockRepository.registerUser(validUser) } returns DataState.Error(errorMessage)

        // Act
        val result = registerUserUseCase(validUser)

        // Assert
        coVerify(exactly = 1) { mockRepository.registerUser(validUser) }
        assertTrue("El resultado debería ser DataState.Error", result is DataState.Error)
        assertEquals(errorMessage, (result as DataState.Error).message)
    }
}