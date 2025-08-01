package com.example.todoapp.domain.logic.sync

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val repository: TaskRepository,
    private val conflictResolver: ConflictResolver,
    private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Sincronización reactiva.
     * 1. Obtiene tareas locales (una vez).
     * 2. Escucha tareas remotas como Flow<DataState<List<Task>>>.
     * 3. Resuelve conflictos.
     * 4. Actualiza local y remoto.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun syncReactive(): Flow<DataState<Unit>> = flow {
        emit(DataState.Loading)

        try {
            val localTasks = repository.getLocalTasks().first() // ← solo los datos actuales

            repository.getRemoteTasks().collect { state ->
                when (state) {
                    is DataState.Loading -> emit(DataState.Loading)

                    is DataState.Success -> {
                        val resolved = conflictResolver.resolve(localTasks, state.data)

                        repository.updateLocalTasks(resolved)
                        repository.updateRemoteTasks(resolved)

                        emit(DataState.Success(Unit))
                    }

                    is DataState.Error -> emit(DataState.Error(state.message))
                    DataState.Idle -> emit(DataState.Idle)
                }
            }

        } catch (e: Exception) {
            emit(DataState.Error(e.localizedMessage ?: "Error syncing tasks"))
        }
    }.flowOn(ioDispatcher)
}