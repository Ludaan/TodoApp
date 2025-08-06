package com.example.todoapp.domain.use_case.auth.params

data class UpdateUserProfileParams(
    val displayName: String?,
    val photoUrl: String?
)