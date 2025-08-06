package com.example.todoapp.domain.use_case.auth

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.params.UpdateUserProfileParams
import javax.inject.Inject

class UpdateUserProfileUseCase @Inject constructor(private val authRepository: FirebaseAuthApi) {

    suspend operator fun invoke(params: UpdateUserProfileParams) : DataState<Unit>{
        if (params.displayName == null && params.photoUrl == null) {
            return DataState.Error("At least one field (displayName or photoUrl) must be provided to update.")
        }
        return authRepository.updateUserProfile(
            params.displayName,
            params.photoUrl)
    }

}