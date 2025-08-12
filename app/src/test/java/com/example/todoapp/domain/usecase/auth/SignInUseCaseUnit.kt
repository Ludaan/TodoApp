package com.example.todoapp.domain.usecase.auth

import android.util.Patterns
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.User
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.SignInUseCase
import com.example.todoapp.domain.use_case.auth.params.SignInParams
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.regex.Matcher
import java.util.regex.Pattern

class SignInUseCaseUnit {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockAuthRepository: FirebaseAuthApi

    private lateinit var signInUseCase: SignInUseCase
    @RelaxedMockK // Mockeamos Matcher
    private lateinit var mockEmailPattern: Pattern // Mock para Patterns.EMAIL_ADDRESS
    private lateinit var mockMatcher: Matcher

    // User de ejemplo para respuestas exitosas
    private val mockUser = User(

        uid = "testUid123",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null,
        isEmailVerified = false
    )

    @Before
    fun setUp() {
        // 1. Preparamos MockK para interceptar llamadas estáticas a la clase Patterns
        mockkStatic(Patterns::class)

        // 2. Creamos mocks para Pattern y Matcher
        mockEmailPattern = mockk<Pattern>()
        mockMatcher = mockk<Matcher>(relaxed = true) // relaxed = true para no mockear todos sus métodos

        // 3. Configuramos el mock estático:
        // Cuando se acceda a Patterns.EMAIL_ADDRESS, devolverá nuestro mockEmailPattern
        every { Patterns.EMAIL_ADDRESS } returns mockEmailPattern

        signInUseCase = SignInUseCase(mockAuthRepository)
    }


    @Test
    fun `invoke con email Y password vacios deberia devolver ERROR_EMAIL_AND_PASSWORD_EMPTY sin llamar al repositorio`() = runTest {
        // Arrange
        val params = SignInParams(email = "", password = "")
        // Este es el primer error que se espera según tu lógica actual
        val expectedErrorMessage = "El correo electrónico y la contraseña no pueden estar vacíos."

        // Act
        val result = signInUseCase(params)

        // Assert
        assertTrue("El resultado debería ser DataState.Error", result is DataState.Error)
        assertEquals(expectedErrorMessage, (result as DataState.Error).message)
        coVerify(exactly = 0) { mockAuthRepository.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `invoke con SOLO email vacio deberia devolver ERROR_EMAIL_EMPTY sin llamar al repositorio`() = runTest {
        // Arrange
        // Aseguramos que el password NO esté vacío para que no caiga en la primera validación combinada.
        val params = SignInParams(email = "", password = "password123")
        val expectedErrorMessage = "El correo electrónico no puede estar vacío."

        // Act
        val result = signInUseCase(params)

        // Assert
        assertTrue("El resultado debería ser DataState.Error", result is DataState.Error)
        assertEquals(expectedErrorMessage, (result as DataState.Error).message)
        coVerify(exactly = 0) { mockAuthRepository.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `invoke con SOLO password vacio deberia devolver ERROR_PASSWORD_EMPTY sin llamar al repositorio`() = runTest {
        // Arrange
        // Aseguramos que el email NO esté vacío para que no caiga en la primera validación combinada.
        val params = SignInParams(email = "test@example.com", password = "")
        val expectedErrorMessage = "La contraseña no puede estar vacía."

        // Act
        val result = signInUseCase(params)

        // Assert
        assertTrue("El resultado debería ser DataState.Error", result is DataState.Error)
        assertEquals(expectedErrorMessage, (result as DataState.Error).message)
        coVerify(exactly = 0) { mockAuthRepository.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `invoke con formato de email invalido (y campos no vacios) deberia devolver ERROR_EMAIL_FORMAT sin llamar al repositorio`() = runTest {
        // Arrange
        val invalidEmail = "emailinvalido"
        val params = SignInParams(email = invalidEmail, password = "password123")
        val expectedErrorMessage = "El formato del correo electrónico no es válido."

        // Aquí está la clave: mockeamos la llamada a Patterns.EMAIL_ADDRESS.matcher(...).matches()
        // Cuando Patterns.EMAIL_ADDRESS.matcher(invalidEmail) se llame, devolverá nuestro mockMatcher
        every { Patterns.EMAIL_ADDRESS.matcher(invalidEmail) } returns mockMatcher
        // Y cuando mockMatcher.matches() se llame, devolverá false (simulando un email inválido)
        every { mockMatcher.matches() } returns false


        // Act
        val result = signInUseCase(params)

        // Assert
        assertTrue("El resultado debería ser DataState.Error", result is DataState.Error)
        assertEquals(expectedErrorMessage, (result as DataState.Error).message)
        coVerify(exactly = 0) { mockAuthRepository.signInWithEmailAndPassword(any(), any()) }
    }

    @Test
    fun `invoke con password demasiado corto (y campos no vacios, email valido) deberia devolver ERROR_PASSWORD_LENGTH sin llamar al repositorio`() = runTest {
        // Arrange
        // Aseguramos que email y password no estén vacíos, y que el email sea válido
        val params = SignInParams(email = "test@example.com", password = "123")
        val expectedErrorMessage = "La contraseña debe tener al menos 6 caracteres."

        // Act
        val result = signInUseCase(params)

        // Assert
        assertTrue("El resultado debería ser DataState.Error", result is DataState.Error)
        assertEquals(expectedErrorMessage, (result as DataState.Error).message)
        coVerify(exactly = 0) { mockAuthRepository.signInWithEmailAndPassword(any(), any()) }
    }


    @Test
    fun `invoke con credenciales validas y repositorio exitoso deberia devolver Success`() = runTest {
        // Arrange
        val validParams = SignInParams(email = "test@example.com", password = "password123")
        // Aquí mockUser debería ser una instancia de tu clase User, por ejemplo:
        // val mockUser = User(userId = "uid123", email = "test@example.com", ...)
        coEvery { mockAuthRepository.signInWithEmailAndPassword(validParams.email, validParams.password) } returns DataState.Success(mockUser) // mockUser definido arriba en la clase

        // Act
        val result = signInUseCase(validParams)

        // Assert
        assertTrue("El resultado debería ser DataState.Success", result is DataState.Success)
        assertEquals(mockUser, (result as DataState.Success).data)
        coVerify(exactly = 1) { mockAuthRepository.signInWithEmailAndPassword(validParams.email, validParams.password) }
    }

    @Test
    fun `invoke con credenciales validas y repositorio falla deberia devolver Error`() = runTest {
        // Arrange
        val validParams = SignInParams(email = "test@example.com", password = "password123")
        val repoErrorMessage = "Error de autenticación desde el repositorio"
        coEvery { mockAuthRepository.signInWithEmailAndPassword(validParams.email, validParams.password) } returns DataState.Error(repoErrorMessage)

        // Act
        val result = signInUseCase(validParams)

        // Assert
        assertTrue("El resultado debería ser DataState.Error", result is DataState.Error)
        assertEquals(repoErrorMessage, (result as DataState.Error).message)
        coVerify(exactly = 1) { mockAuthRepository.signInWithEmailAndPassword(validParams.email, validParams.password) }
    }
}