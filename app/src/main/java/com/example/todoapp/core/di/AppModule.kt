package com.example.todoapp.core.di

import android.content.Context
import androidx.room.Room
import com.example.todoapp.core.common.DispatchersProvider
import com.example.todoapp.core.common.StandardDispatchers
import com.example.todoapp.data.local.AppDatabase
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.domain.repository.TaskRepository
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

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    // 1. Dispatchers (Binds)
    @Binds
    @Singleton
    abstract fun bindDispatchers(dispatchers: StandardDispatchers): DispatchersProvider

    // 2. Repository (Binds)
    //@Binds
    //@Singleton
   // abstract fun bindTaskRepository(repository: TaskRepositoryImpl): TaskRepository

    // 3. Módulo de compañía para provides
    @InstallIn(SingletonComponent::class)
    companion object {
        // Database
        @Provides
        @Singleton
        fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "tasks_db"
            ).fallbackToDestructiveMigration().build()
        }

        @Provides
        fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

        // Firebase
        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore = Firebase.firestore

        // Mapper (si no tiene interfaz)
       // @Provides
       // @Singleton
        //fun provideTaskMapper(): TaskMapper = TaskMapper()

        // WorkManager
       // @Provides
        //@Singleton
       // fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
       //     return WorkManager.getInstance(context)
      //  }
    }
}

