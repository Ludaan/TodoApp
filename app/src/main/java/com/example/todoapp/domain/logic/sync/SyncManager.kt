package com.example.todoapp.domain.logic.sync

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.SyncResult
import com.example.todoapp.domain.repository.TaskSyncGateway
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val syncGateway: TaskSyncGateway,
    private val conflictResolver: ConflictResolver,
    private val ioDispatcher: CoroutineDispatcher
) {

    /**
     * Sincronización reactiva.
     * 1. Escucha tareas remotas como Flow<DataState<List<Task>>>.
     * 2. Toma snapshot local y resuelve conflictos por updatedAt.
     * 3. Sincroniza pendientes y borrados pendientes.
     * 4. Marca estado SYNCED/FAILED según resultado.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun syncReactive(): Flow<DataState<SyncResult>> = flow {
        emit(DataState.Loading)

        try {
            syncGateway.observeRemoteTasks().collect { state ->
                when (state) {
                    is DataState.Loading -> emit(DataState.Loading)

                    is DataState.Success -> {
                        val localTasks = syncGateway.getLocalTasksSnapshot()
                        val resolvedTasks = conflictResolver.resolve(localTasks, state.data)
                        syncGateway.replaceLocalTasks(resolvedTasks)
                        val result = syncGateway.syncResolvedTasks(resolvedTasks)
                        emit(DataState.Success(result))
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
