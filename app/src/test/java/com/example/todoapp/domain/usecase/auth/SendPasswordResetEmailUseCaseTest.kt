package com.example.todoapp.domain.usecase.auth

import android.util.Patterns
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.SendPasswordResetEmailUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.mockk // IMPORTANTE: Necesitamos mockk() de nuevo
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.regex.Matcher
import java.util.regex.Pattern

class SendPasswordResetEmailUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockAuthRepository: FirebaseAuthApi

    // Volver a la declaración sin @RelaxedMockK para estos
    private lateinit var mockActualEmailPattern: Pattern
    private lateinit var mockMatcherForValidation: Matcher

    private lateinit var sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase

    private val ERROR_EMAIL_EMPTY = "El correo electrónico no puede estar vacío."
    private val ERROR_EMAIL_FORMAT = "El formato del correo electrónico no es válido."

    @Before
    fun setUp() {
        // 1. Habilitar mockeo estático ANTES de cualquier otra cosa con Patterns
        mockkStatic(Patterns::class)

        // 2. Crear los mocks para Pattern y Matcher MANUALMENTE AQUÍ,
        //    DESPUÉS de mockkStatic y ANTES del 'every' problemático.
        //    Esto replica exactamente la estructura de SignInUseCaseUnit.kt
        mockActualEmailPattern = mockk<Pattern>()
        mockMatcherForValidation = mockk<Matcher>(relaxed = true)

        // 3. Esta es la línea problemática. Ahora 'mockActualEmailPattern'
        //    es un mock creado justo después de que 'Patterns' fue modificado por mockkStatic.
       // every { Patterns.EMAIL_ADDRESS } returns mockActualEmailPattern

        // 4. Inicializar el UseCase
        sendPasswordResetEmailUseCase = SendPasswordResetEmailUseCase(mockAuthRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Patterns::class)
    }

    private fun setupMockedEmailValidation(emailToValidate: String, isEmailFormatValid: Boolean) {
        every { mockActualEmailPattern.matcher(emailToValidate) } returns mockMatcherForValidation
        every { mockMatcherForValidation.matches() } returns isEmailFormatValid
    }

    // ... (El resto de tus tests permanecen igual) ...
    // Los copio aquí para que el archivo esté completo.

    @Test
    fun `invoke con email vacio deberia devolver ERROR_EMAIL_EMPTY sin llamar al repositorio`() = runTest {
        // Arrange
        val emptyEmail = ""
        // Act
        val result = sendPasswordResetEmailUseCase(emptyEmail)
        // Assert
        assertTrue("El resultado debería ser DataState.Error", result is DataState.Error)
        assertEquals(ERROR_EMAIL_EMPTY, (result as DataState.Error).message)
        coVerify(exactly = 0) { mockAuthRepository.sendPasswordResetEmail(any()) }
    }

    @Test
    fun `invoke con email solo con espacios deberia devolver ERROR_EMAIL_EMPTY sin llamar al repositorio`() = runTest {
        // Arrange
        val blankEmail = "   "
        // Act
        val result = sendPasswordResetEmailUseCase(blankEmail)
        // Assert
        assertTrue("El resultado debería ser DataState.Error", result is DataState.Error)
        assertEquals(ERROR_EMAIL_EMPTY, (result as DataState.Error).message)
        coVerify(exactly = 0) { mockAuthRepository.sendPasswordResetEmail(any()) }
    }


}

