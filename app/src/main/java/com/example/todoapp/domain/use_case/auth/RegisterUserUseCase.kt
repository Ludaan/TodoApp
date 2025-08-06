package com.example.todoapp.domain.use_case.auth

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.User
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.params.RegisterUserParams
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(private val authRepository: FirebaseAuthApi) {

    suspend operator fun invoke(params: RegisterUserParams) : DataState<User>{
        if (params.email.isBlank() || params.password.isBlank() || params.username.isBlank()){
            return DataState.Error("Email, password, and username cannot be blank.")
        }
        return authRepository.registerUser(params)
    }
}