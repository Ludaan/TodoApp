package com.example.todoapp.presentation.features.auth.login

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.use_case.auth.RegisterUserUseCase
import com.example.todoapp.domain.use_case.auth.SendPasswordResetEmailUseCase
import com.example.todoapp.domain.use_case.auth.SignInUseCase
import com.example.todoapp.domain.use_case.auth.params.RegisterUserParams
import com.example.todoapp.domain.use_case.auth.params.SignInParams
import com.example.todoapp.domain.use_case.task.ClearLocalTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val signInUseCase: SignInUseCase,
    private val registerUserUseCase: RegisterUserUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val clearLocalTasksUseCase: ClearLocalTasksUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String){
        _uiState.update { it.copy(emailInput = email, userMessage = null, authState = DataState.Idle) }
    }

    fun onPasswordChanged(email: String){
        _uiState.update { it.copy(passwordInput = email, userMessage = null, authState = DataState.Idle) }
    }

    fun onUserNameChanged(email: String){
        _uiState.update { it.copy(usernameInput = email, userMessage = null, authState = DataState.Idle) }
    }

    fun onUsernameChanged(username: String) { // Para registro
        _uiState.update { it.copy(usernameInput = username, userMessage = null, authState = DataState.Idle) }
    }

    fun toggleRegistrationMode(isRegistering: Boolean) {
        _uiState.update {
            it.copy(
                isRegistrationMode = isRegistering,
                emailInput = "",
                passwordInput = "",
                usernameInput = "",
                userMessage = null,
                authState = DataState.Idle,
                passwordResetState = DataState.Idle
            )
        }
    }

    fun submit() {
        if (_uiState.value.isRegistrationMode) {
            registerUser()
        } else {
            signInUser()
        }
    }

    private fun signInUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authState = DataState.Idle) }
            val result = signInUseCase(
                params = SignInParams(
                    email = _uiState.value.emailInput.trim(),
                    password = _uiState.value.passwordInput
                )
            )
            if (result is DataState.Success) {
                clearLocalTasksUseCase()
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    authState = result,
                    userMessage = if (result is DataState.Error) result.message else null
                )
            }
        }
    }

    private fun registerUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, authState = DataState.Idle) }
            val result = registerUserUseCase(
                params = RegisterUserParams(
                    email = _uiState.value.emailInput.trim(),
                    password = _uiState.value.passwordInput,
                    username = _uiState.value.usernameInput.trim()
                )
            )
            if (result is DataState.Success) {
                clearLocalTasksUseCase()
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    authState = result,
                    userMessage = if (result is DataState.Error) result.message else null
                )
            }
        }
    }

    fun sendPasswordResetEmail() {
        val email = _uiState.value.emailInput.trim()
        if (email.isBlank()) {
            _uiState.update { it.copy(userMessage = SendPasswordResetEmailUseCase.ERROR_EMAIL_EMPTY) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, passwordResetState = DataState.Idle) }
            val result = sendPasswordResetEmailUseCase(email)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    passwordResetState = result,
                    userMessage = when (result) {
                        is DataState.Error -> result.message
                        is DataState.Success -> "Password reset email sent."
                        else -> null
                    }
                )
            }
        }
    }

    fun userMessageShown() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun resetAuthState() {
        _uiState.update { it.copy(authState = DataState.Idle, passwordResetState = DataState.Idle) }
    }

}
