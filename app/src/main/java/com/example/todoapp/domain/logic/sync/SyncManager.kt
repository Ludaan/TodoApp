package com.example.todoapp.domain.logic.sync

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.TaskSyncStatus
import com.example.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
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
     * 1. Escucha tareas remotas como Flow<DataState<List<Task>>>.
     * 2. Toma snapshot local y resuelve conflictos por updatedAt.
     * 3. Sincroniza pendientes y borrados pendientes.
     * 4. Marca estado SYNCED/FAILED según resultado.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun syncReactive(): Flow<DataState<Unit>> = flow {
        emit(DataState.Loading)

        try {
            repository.getRemoteTasks().collect { state ->
                when (state) {
                    is DataState.Loading -> emit(DataState.Loading)

                    is DataState.Success -> {
                        val localTasks = repository.getLocalTasksSnapshot()
                        val resolved = conflictResolver.resolve(localTasks, state.data)
                        repository.updateLocalTasks(resolved)
                        repository.updateRemoteTasks(resolved)
                        syncPendingWrites()
                        syncPendingDeletes()

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

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun syncPendingWrites() {
        repository.getPendingTasks().forEach { task ->
            try {
                repository.addRemoteTask(task.copy(syncStatus = TaskSyncStatus.SYNCED))
                repository.ackSynced(task.id, System.currentTimeMillis())
            } catch (_: Exception) {
                repository.markFailed(task.id, System.currentTimeMillis())
            }
        }
    }

    private suspend fun syncPendingDeletes() {
        repository.getPendingDeleteTaskIds().forEach { taskId ->
            try {
                repository.deleteRemoteTask(taskId)
                repository.deleteLocalTaskHard(taskId)
            } catch (_: Exception) {
                repository.markDeleteFailed(taskId, System.currentTimeMillis())
            }
        }
    }
}
