package com.example.todoapp.data.remote.auth

import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.remote.model.RegisterRequestDto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await
import androidx.core.net.toUri
import com.example.todoapp.domain.repository.FirebaseAuthApi

class FirebaseAuthApiImpl(
    private val firebaseAuth: FirebaseAuth
) : FirebaseAuthApi {
    override suspend fun registerUser(requestDto: RegisterRequestDto): DataState<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(
                requestDto.email,
                requestDto.password
            ).await()
            DataState.Success(authResult.user!!)
        } catch (e: Exception) {
            DataState.Error(e.toString())
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
    ): DataState<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            DataState.Success(authResult.user!!)
        } catch (e: Exception) {
            DataState.Error(e.toString())
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

    override fun getCurrentFirebaseUser(): FirebaseUser? {
        return firebaseAuth.currentUser    }
}