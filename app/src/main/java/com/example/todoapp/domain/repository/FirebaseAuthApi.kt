package com.example.todoapp.domain.repository

import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.remote.model.RegisterRequestDto
import com.example.todoapp.domain.model.User
import com.example.todoapp.domain.use_case.auth.params.RegisterUserParams
import com.google.firebase.auth.FirebaseUser

interface FirebaseAuthApi {
    suspend fun registerUser(params: RegisterUserParams) : DataState<User>
    suspend fun updateUserProfile(displayName: String?, photoUrl: String? = null): DataState<Unit>
    suspend fun signInWithEmailAndPassword(email: String, password: String): DataState<User>
    suspend fun sendPasswordResetEmail(email: String): DataState<Unit>
    suspend fun signOut(): DataState<Unit> // Puede ser suspend o no, y puede devolver Unit o DataState<Unit>
    fun getCurrentFirebaseUser(): User? // Síncrono, ya que Firebase.auth.currentUser es síncrono
}