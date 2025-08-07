package com.example.todoapp.domain.use_case.auth

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.repository.FirebaseAuthApi
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val authRepository: FirebaseAuthApi
) {

    suspend operator fun invoke(): DataState<Unit> {
        return authRepository.signOut()
    }
}