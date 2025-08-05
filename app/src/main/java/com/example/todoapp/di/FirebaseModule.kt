package com.example.todoapp.di

import com.example.todoapp.data.remote.auth.FirebaseAuthApi
import com.example.todoapp.data.remote.auth.FirebaseAuthApiImpl
import com.example.todoapp.data.remote.task.FirebaseTaskApi
import com.example.todoapp.data.remote.task.FirebaseTaskApiImpl
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return Firebase.auth
    }

    @Provides
    @Singleton
    fun provideFirebaseAuthApi(auth: FirebaseAuth): FirebaseAuthApi {
        return FirebaseAuthApiImpl(auth)
    }

    // --- Firebase Firestore ---
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return Firebase.firestore
    }

    // Ya que FirebaseTaskApiImpl depende de FirebaseFirestore,
    // es lógico proveerlo aquí también.
    @Provides
    @Singleton // Si FirebaseTaskApiImpl es stateless y solo depende de Firestore, Singleton es apropiado.
    // Si tuviera algún estado que no debería ser singleton, reconsidera el alcance.
    fun provideFirebaseTaskApi(firestore: FirebaseFirestore): FirebaseTaskApi {
        return FirebaseTaskApiImpl(firestore)
    }
}