package com.example.todoapp.domain.use_case.auth

import android.util.Patterns
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.User
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.params.SignInParams
import javax.inject.Inject

class SignInUseCase @Inject constructor(private val authRepository: FirebaseAuthApi) {

    companion object {
        private const val ERROR_EMAIL_EMPTY = "El correo electrónico no puede estar vacío."
        private const val ERROR_EMAIL_FORMAT = "El formato del correo electrónico no es válido."
        private const val ERROR_PASSWORD_EMPTY = "La contraseña no puede estar vacía."
        private const val ERROR_PASSWORD_LENGTH = "La contraseña debe tener al menos 6 caracteres."
        private const val ERROR_EMAIL_AND_PASSWORD_EMPTY = "El correo electrónico y la contraseña no pueden estar vacíos."
    }

    suspend operator fun invoke(params: SignInParams): DataState<User> {


        if (params.email.isBlank() && params.password.isBlank()) {
            return DataState.Error(ERROR_EMAIL_AND_PASSWORD_EMPTY)
        }

        if (params.email.isBlank()) {
            return DataState.Error(ERROR_EMAIL_EMPTY)
        }
        if (params.password.isBlank()) {
            return DataState.Error(ERROR_PASSWORD_EMPTY)
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(params.email).matches()) {
            return DataState.Error(ERROR_EMAIL_FORMAT)
        }

        if (params.password.length < 6) {
            return DataState.Error(ERROR_PASSWORD_LENGTH)
        }



        return authRepository.signInWithEmailAndPassword(
            email = params.email,
            password = params.password
        )
    }
}