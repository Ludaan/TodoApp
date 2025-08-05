package com.example.todoapp.di

import android.content.Context
import androidx.room.Room
import com.example.todoapp.core.common.DispatchersProvider
import com.example.todoapp.core.common.StandardDispatchers
import com.example.todoapp.data.local.AppDatabase
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.domain.logic.sync.ConflictResolver
import com.example.todoapp.domain.logic.sync.SyncManager
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.data.repository.TaskRepositoryImpl
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

    @Binds
    @Singleton
    abstract fun bindDispatchers(dispatchers: StandardDispatchers): DispatchersProvider

    @Binds
    @Singleton
    abstract fun bindTaskRepository(repository: TaskRepositoryImpl): TaskRepository
    // NOTA: TaskRepositoryImpl ahora obtendrá FirebaseTaskApi de FirebaseModule
    // y TaskDao de AppProvidesModule a través de la inyección de Hilt.
}

// AppProvidesModule.kt (Objeto para @Provides)
@Module
@InstallIn(SingletonComponent::class)
object AppProvidesModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "tasks_db")
            .fallbackToDestructiveMigration(false) // Considera una estrategia de migración adecuada para producción
            .build()

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()


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
