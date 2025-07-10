package com.example.todoapp.presentation.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.use_case.task.AddTaskUseCase
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
    private val addTaskUseCase: AddTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
    private val syncTasksUseCase: SyncTasksUseCase
) : ViewModel() {

    // 🔹 Tareas locales observadas en tiempo real desde Room
    private val _tasks = getTasksUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tasks: StateFlow<List<Task>> = _tasks

    // 🔹 Estado de sincronización con Firebase
    private val _syncState = MutableStateFlow<DataState<Unit>>(DataState.Success(Unit))
    val syncState: StateFlow<DataState<Unit>> = _syncState

    // 🔹 Agregar tarea (local + remoto)
    fun addTask(task: Task) {
        viewModelScope.launch {
            addTaskUseCase(task)
        }
    }

    // 🔹 Eliminar tarea (local + remoto)
    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            deleteTaskUseCase(taskId)
        }
    }

    // 🔹 Sincronización con Firebase
    @RequiresApi(Build.VERSION_CODES.O)
    fun syncTasks() {
        viewModelScope.launch {
            syncTasksUseCase().collect { state ->
                _syncState.value = state
            }
        }
    }
}