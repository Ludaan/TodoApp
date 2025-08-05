package com.example.todoapp.domain.use_case.auth

import com.example.todoapp.domain.repository.FirebaseAuthApi
import javax.inject.Inject

class RegisterUserCase @Inject constructor(private val authRepository: FirebaseAuthApi) {

    suspend operator fun invoke(params: Register)
}