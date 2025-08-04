package com.example.todoapp.data.remote.model

data class RegisterRequestDto(
    val email: String,
    val password: String,
    val username: String,
)
