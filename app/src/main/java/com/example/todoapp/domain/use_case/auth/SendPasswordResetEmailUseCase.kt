package com.example.todoapp.domain.use_case.auth

import android.util.Patterns
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.google.firebase.auth.oAuthProvider
import javax.inject.Inject

class SendPasswordResetEmailUseCase  @Inject constructor(
    private val authRepository: FirebaseAuthApi
){

    companion object {
        const val ERROR_EMAIL_EMPTY = "El correo electrónico no puede estar vacío."
        const val ERROR_EMAIL_FORMAT = "El formato del correo electrónico no es válido."
    }

    suspend operator fun invoke(email: String) : DataState<Unit> {

        if (email.isBlank()){
            return DataState.Error(ERROR_EMAIL_EMPTY)
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return DataState.Error(ERROR_EMAIL_FORMAT)
        }

        return authRepository.sendPasswordResetEmail(email)

    }

}