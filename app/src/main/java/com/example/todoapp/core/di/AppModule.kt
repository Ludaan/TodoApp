package com.example.todoapp.core.di

import android.content.Context
import androidx.room.Room
import com.example.todoapp.core.common.DispatchersProvider
import com.example.todoapp.core.common.StandardDispatchers
import com.example.todoapp.data.local.AppDatabase
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.remote.api.FirebaseTaskApi
import com.example.todoapp.data.remote.api.FirebaseTaskApiImpl
import com.example.todoapp.data.sync.ConflictResolver
import com.example.todoapp.data.sync.SyncManager
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.domain.repository.TaskRepositoryImpl
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// AppModule.kt
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindDispatchers(dispatchers: StandardDispatchers): DispatchersProvider

    @Binds
    @Singleton
    abstract fun bindTaskRepository(repository: TaskRepositoryImpl): TaskRepository
}

// AppProvidesModule.kt
@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "tasks_db")
            .fallbackToDestructiveMigration(false)
            .build()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    @Singleton
    fun provideFireStore(): FirebaseFirestore = Firebase.firestore

    @Provides
    fun provideFirebaseTaskApi(firestore: FirebaseFirestore): FirebaseTaskApi =
        FirebaseTaskApiImpl(firestore)

    @Provides
    @Singleton
    fun provideConflictResolver(): ConflictResolver = ConflictResolver()

    @Provides
    @Singleton
    fun provideSyncManager(
        repository: TaskRepository,
        conflictResolver: ConflictResolver,
        dispatchers: DispatchersProvider
    ): SyncManager = SyncManager(repository, conflictResolver, dispatchers.io)
}

