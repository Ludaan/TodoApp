package com.example.todoapp.domain.use_case.auth

import com.example.todoapp.domain.model.User
import com.example.todoapp.domain.repository.FirebaseAuthApi
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository : FirebaseAuthApi
) {

    suspend operator fun invoke() : User? {
        return authRepository.getCurrentFirebaseUser()
    }
}