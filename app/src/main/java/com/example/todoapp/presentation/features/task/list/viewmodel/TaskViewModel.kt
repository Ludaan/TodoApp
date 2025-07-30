package com.example.todoapp.presentation.features.task.list.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.use_case.task.DeleteTaskUseCase
import com.example.todoapp.domain.use_case.task.GetTaskUseCase
import com.example.todoapp.domain.use_case.sync.SyncTasksUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    getTasksUseCase: GetTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val syncTasksUseCase: SyncTasksUseCase
) : ViewModel() {

    private val _userMessage = MutableStateFlow<String?>(null)
    private val _syncState = MutableStateFlow<DataState<Unit>>(DataState.Idle)
    private val _isLoadingTasks = MutableStateFlow(true)

    // Combina los diferentes flujos de datos en un solo UiState
    val uiState: StateFlow<TaskListUiState> = combine(
        getTasksUseCase() // Este es el StateFlow<List<Task>> que ya tenías
            .onStart { _isLoadingTasks.value = true } // Aunque getTasksUseCase es un StateFlow,
            // podrías querer resetear isLoadingTasks
            // si el flujo se reinicia por alguna razón.
            // O simplemente manejar la carga inicial una vez.
            .map { tasks -> _isLoadingTasks.value = false; tasks }, // Desactivar carga cuando llegan tareas
        _isLoadingTasks,
        _syncState,
        _userMessage
    ) { tasks, isLoading, syncState, userMessage ->
        TaskListUiState(
            tasks = tasks,
            isLoadingTasks = isLoading,
            syncStatus = syncState,
            userMessage = userMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskListUiState(isLoadingTasks = true) // Estado inicial
    )

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                // Suponiendo que deleteTaskUseCase podría lanzar una excepción o devolver un resultado
                deleteTaskUseCase(taskId)
                // Opcional: Mostrar un mensaje de éxito
                // _userMessage.value = "Tarea eliminada correctamente"
            } catch (e: Exception) {
                // Manejar el error, por ejemplo, mostrando un mensaje
                _userMessage.value = "Error al eliminar la tarea: ${e.message}"
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun syncTasks() {
        viewModelScope.launch {
            syncTasksUseCase()
                .onStart { _syncState.value = DataState.Loading }
                .catch { e ->
                    _syncState.value = DataState.Error("Sincronización fallida: ${e.message}")
                }
                .collect { state ->
                    _syncState.value = state
                    if (state is DataState.Success) {
                        // Opcional: mensaje de éxito para la sincronización
                        // _userMessage.value = "Tareas sincronizadas"
                    } else if (state is DataState.Error) {
                        _userMessage.value = state.message // Mostrar mensaje de error de sincronización
                    }
                }
        }
    }

    fun userMessageShown() {
        _userMessage.value = null
    }
}