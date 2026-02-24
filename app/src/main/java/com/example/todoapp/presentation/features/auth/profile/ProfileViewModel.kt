package com.example.todoapp.presentation.features.auth.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.use_case.auth.GetCurrentUserUseCase
import com.example.todoapp.domain.use_case.auth.SignOutUseCase
import com.example.todoapp.domain.use_case.auth.UpdateUserProfileUseCase
import com.example.todoapp.domain.use_case.auth.params.UpdateUserProfileParams
import com.example.todoapp.domain.use_case.task.ClearLocalTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val clearLocalTasksUseCase: ClearLocalTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser(){
        _uiState.update { it.copy(isLoading = true) }
        val user = getCurrentUserUseCase()
        _uiState.update { it.copy(currentUser = user, isLoading = false, displayNameInput = user?.displayName ?: "", photoUrlInput = user?.photoUrl ?: "") }
        if (user == null){
            _uiState.update { it.copy(userMessage = "No user session found.") }
        }
    }

    fun onDisplayNameChanged(displayName: String) {
        _uiState.update { it.copy(displayNameInput = displayName, updateProfileState = DataState.Idle) }
    }


    fun updateUserProfile() {
        val currentDisplayName = _uiState.value.currentUser?.displayName
        val newDisplayName = _uiState.value.displayNameInput.trim()
        val newPhotoUrl = _uiState.value.photoUrlInput.trim()

        val params = UpdateUserProfileParams(
            displayName = if (newDisplayName != currentDisplayName && newDisplayName.isNotEmpty()) newDisplayName else null,
             photoUrl = newPhotoUrl.ifEmpty { null }
        )

        if (params.displayName == null) {
            _uiState.update { it.copy(userMessage = "No changes to update.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingProfile = true, updateProfileState = DataState.Idle) }
            val result = updateUserProfileUseCase(params)
            _uiState.update {
                it.copy(
                    isUpdatingProfile = false,
                    updateProfileState = result,
                    userMessage = if (result is DataState.Error) result.message else "Profile updated successfully."
                )
            }
            if (result is DataState.Success) {
                loadCurrentUser()
            }
        }
    }


    fun signOut() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = signOutUseCase()
            if (result is DataState.Success) {
                clearLocalTasksUseCase()
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    signOutState = result,
                    userMessage = if (result is DataState.Error) result.message else null
                )
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun resetUpdateState() {
        _uiState.update { it.copy(updateProfileState = DataState.Idle) }
    }

}
