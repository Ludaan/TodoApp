package com.example.todoapp.domain.use_case.auth.params

data class RegisterUserParams(
    val email: String,
    val password: String,
    val username: String
)
