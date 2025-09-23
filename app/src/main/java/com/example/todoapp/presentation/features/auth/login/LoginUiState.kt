package com.example.todoapp.presentation.features.auth.login

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.User

data class LoginUiState(
    val emailInput : String = "",
    val passwordInput : String = "",
    val usernameInput : String = "",
    val isLoading: Boolean = false,
    val authState: DataState<User> = DataState.Idle,
    val passwordResetState: DataState<Unit> = DataState.Idle,
    val userMessage: String? = null,
    val isRegistrationMode: Boolean = false
)
