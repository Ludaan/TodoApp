package com.example.todoapp.core.di

import android.content.Context
import androidx.room.Room
import com.example.todoapp.core.common.DispatchersProvider
import com.example.todoapp.core.common.StandardDispatchers
import com.example.todoapp.data.local.AppDatabase
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.domain.repository.TaskRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tasks_Db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    // Dispatchers
    @Provides
    @Singleton
    fun provideDispatchers(): DispatchersProvider = StandardDispatchers()



}

