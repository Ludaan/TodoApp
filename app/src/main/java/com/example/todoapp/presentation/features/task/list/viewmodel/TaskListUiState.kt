package com.example.todoapp.presentation.features.task.list.viewmodel

import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.Task

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoadingTasks: Boolean = false, // Para la carga inicial de tareas
    val syncStatus: DataState<Unit> = DataState.Idle, // Estado de la sincronización
    val userMessage: String? = null // Para mensajes al usuario (errores, éxito)
)
