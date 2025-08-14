package com.example.todoapp.domain.usecase.auth

import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.GetCurrentUserUseCase
import com.example.todoapp.domain.model.User
import io.mockk.every
import org.junit.Assert.assertEquals
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.verify
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetCurrentUserUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockAuthRepository: FirebaseAuthApi
    private lateinit var getCurrentUserUseCase: GetCurrentUserUseCase

    @Before
    fun setUp(){
        getCurrentUserUseCase = GetCurrentUserUseCase(mockAuthRepository)
    }

    @Test
    fun `invoke cuando el repositorio devuelve un usuarion, deberia devolver el usuario`() {

        val mockUser =  User(uid = "uid123", email = "test@example.com", displayName = "Test User", photoUrl = null)
        every { mockAuthRepository.getCurrentFirebaseUser() } returns mockUser

        //Act
        val result = getCurrentUserUseCase()

        //Assert
        assertEquals(mockUser, result)
        verify(exactly = 1) { mockAuthRepository.getCurrentFirebaseUser() }

    }

    @Test
    fun `invoke cuando el repositorio devuelve null, deberia devolver null`(){
        every { mockAuthRepository.getCurrentFirebaseUser() } returns null

        //Act
        val result = getCurrentUserUseCase()

        //Asset

        assertNull(result)
        verify(exactly = 1) {  mockAuthRepository.getCurrentFirebaseUser() }

    }


}