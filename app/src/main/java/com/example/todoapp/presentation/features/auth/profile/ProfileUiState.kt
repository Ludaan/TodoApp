package com.example.todoapp.presentation.features.auth.profile

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.User

data class ProfileUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
    val displayNameInput: String = "",
    val photoUrlInput: String = "",
    val isUpdatingProfile: Boolean = false,
    val updateProfileState: DataState<Unit> = DataState.Idle,
    val signOutState: DataState<Unit> = DataState.Idle,
    val userMessage: String? = null
)
