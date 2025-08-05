package com.example.todoapp.domain.repository

import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.remote.model.RegisterRequestDto
import com.google.firebase.auth.FirebaseUser

interface FirebaseAuthApi {
    suspend fun registerUser(requestDto: RegisterRequestDto) : DataState<FirebaseUser>
    suspend fun updateUserProfile(displayName: String?, photoUrl: String? = null): DataState<Unit>
    suspend fun signInWithEmailAndPassword(email: String, password: String): DataState<FirebaseUser>
    suspend fun sendPasswordResetEmail(email: String): DataState<Unit>
    suspend fun signOut(): DataState<Unit> // Puede ser suspend o no, y puede devolver Unit o DataState<Unit>
    fun getCurrentFirebaseUser(): FirebaseUser? // Síncrono, ya que Firebase.auth.currentUser es síncrono
}