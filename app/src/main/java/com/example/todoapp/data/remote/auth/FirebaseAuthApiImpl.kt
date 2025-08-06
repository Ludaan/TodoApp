package com.example.todoapp.data.remote.auth

import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.remote.model.RegisterRequestDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri
import com.example.todoapp.data.mapper.toDomainUser
import com.example.todoapp.domain.model.User
import com.example.todoapp.domain.repository.FirebaseAuthApi
import com.example.todoapp.domain.use_case.auth.params.RegisterUserParams
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException

class FirebaseAuthApiImpl(
    private val firebaseAuth: FirebaseAuth
) : FirebaseAuthApi {
    override suspend fun registerUser(params: RegisterUserParams): DataState<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(
                params.email,    // Usa params.email
                params.password  // Usa params.password
            ).await()
            val firebaseUser: FirebaseUser = authResult.user
                ?: return DataState.Error("Firebase user creation failed, user is null.") // Obtener FirebaseUser de AuthResult

            if (params.username.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(params.username) // Usa params.username
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            }
            val domainUser = firebaseUser.toDomainUser()

            if (domainUser != null) {
                DataState.Success(domainUser)
            } else {
                // Esto podría ocurrir si el mapeo falla por alguna razón, o si firebaseUser fuera nulo
                // aunque ya lo comprobamos.
                DataState.Error("Failed to map Firebase user to domain user.")
            }


        } catch (e: FirebaseAuthUserCollisionException) { // Email ya en uso
            DataState.Error("Este correo electrónico ya está registrado. Intenta con otro.")
        } catch (e: FirebaseAuthInvalidCredentialsException) { // Formato de email inválido, contraseña débil
            DataState.Error("Credenciales inválidas. Asegúrate de que el correo sea válido y la contraseña sea segura.")
        } catch (e: Exception) { // Captura genérica para otros errores
            // Podrías loggear 'e' para depuración
            DataState.Error("Ocurrió un error durante el registro: ${e.localizedMessage ?: "Error desconocido"}")
        }
    }

    override suspend fun updateUserProfile(
        displayName: String?,
        photoUrl: String?
    ): DataState<Unit> {
        return try {
            val user = firebaseAuth.currentUser
                ?: return DataState.Error("No user is currently signed in to update profile.")

            val profileUpdatesBuilder = UserProfileChangeRequest.Builder()
            displayName?.let { profileUpdatesBuilder.setDisplayName(it) }
            photoUrl?.let { profileUpdatesBuilder.setPhotoUri(it.toUri()) }
            val profileUpdates = profileUpdatesBuilder.build()

            if (profileUpdates.displayName != null || profileUpdates.photoUri != null) {
                user.updateProfile(profileUpdates).await()
            }
            DataState.Success(Unit)
        }catch (e: Exception){
            DataState.Error(e.toString())
        }
    }

    override suspend fun signInWithEmailAndPassword(
        email: String,
        password: String
    ): DataState<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
                ?: return DataState.Error("Error al iniciar sesión, usuario de Firebase nulo.")

            val domainUser = firebaseUser.toDomainUser()
            if (domainUser != null) {
                DataState.Success(domainUser)
            } else {
                DataState.Error("Error al mapear el usuario de Firebase después del inicio de sesión.")
            }
        } catch (e: FirebaseAuthInvalidUserException) { // Usuario no encontrado
            DataState.Error("No se encontró un usuario con este correo electrónico.")
        } catch (e: FirebaseAuthInvalidCredentialsException) { // Contraseña incorrecta
            DataState.Error("La contraseña es incorrecta.")
        } catch (e: Exception) { // Captura genérica para otros errores
            // Podrías loggear 'e' para depuración
            DataState.Error("Ocurrió un error al iniciar sesión: ${e.localizedMessage ?: "Error desconocido"}")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): DataState<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            DataState.Success(Unit)
        } catch (e: Exception) {
            DataState.Error(e.toString())
        }
    }

    override suspend fun signOut(): DataState<Unit> {
        return try {
            firebaseAuth.signOut()
            DataState.Success(Unit)
        } catch (e: Exception) {
            DataState.Error(e.toString())
        }    }

    override fun getCurrentFirebaseUser(): User? {
        return firebaseAuth.currentUser.toDomainUser()
    }
}