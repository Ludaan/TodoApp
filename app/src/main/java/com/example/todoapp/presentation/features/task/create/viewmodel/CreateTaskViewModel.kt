package com.example.todoapp.presentation.features.task.create.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.use_case.task.AddTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CreateTaskViewModel @Inject constructor(
    private val addTaskUseCase: AddTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy",
        java.util.Locale("es", "MX")
    )
        .withZone(ZoneId.systemDefault())

    init {
        _uiState.update { currentState ->
            currentState.copy(endRepeatDateText = dateFormatter.format(currentState.endRepeatDate))
        }
    }

    // --- Funciones para manejar eventos de la UI (sin cambios) ---
    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(title = newTitle, saveSuccess = false) }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription, saveSuccess = false) }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { it.copy(selectedCategory = category, isCategoryDropdownExpanded = false, saveSuccess = false) }
    }

    fun onCategoryDropdownDismiss() {
        _uiState.update { it.copy(isCategoryDropdownExpanded = false) }
    }

    fun onCategoryDropdownExpandedChange(expanded: Boolean) {
        _uiState.update { it.copy(isCategoryDropdownExpanded = expanded) }
    }

    fun onColorSelected(color: Color) {
        _uiState.update { it.copy(selectedColor = color, saveSuccess = false) }
    }

    fun onRepeatDailyChange(repeat: Boolean) {
        _uiState.update { it.copy(repeatDaily = repeat, saveSuccess = false) }
    }

    fun onShowTimePickerChange(show: Boolean) {
        _uiState.update { it.copy(showTimePicker = show) }
    }

    fun onRepeatTimeChange(time: LocalTime) {
        _uiState.update { it.copy(repeatTime = time, saveSuccess = false) }
    }

    fun onEndRepeatDateChange(date: Instant) {
        _uiState.update {
            it.copy(
                endRepeatDate = date,
                endRepeatDateText = dateFormatter.format(date),
                saveSuccess = false
            )
        }
    }

    fun onEndRepeatOptionChange(option: String) {
        _uiState.update { it.copy(endRepeatOption = option, saveSuccess = false) }
    }

    fun saveTask() {
        val currentState = _uiState.value // Obtenemos la instantánea actual del UiState

        // Validaciones básicas (opcional pero recomendado)
        if (currentState.title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El título no puede estar vacío") } // Necesitarías añadir errorMessage a tu UiState
            return
        }

        if (currentState.isSaving) return // Evitar múltiples clics si ya se está guardando

        _uiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }

        // Construir el objeto Task a partir del UiState
        val taskToSave = Task(
            id = UUID.randomUUID().toString(), // Generar un nuevo ID único para la tarea
            title = currentState.title,
            description = currentState.description,
            isCompleted = false, // Las nuevas tareas generalmente no están completadas
            createdAt = Instant.now(), // Fecha y hora actual de creación
            color = currentState.selectedColor.toArgb(), // Convierte Compose Color a Int ARGB
            limitDate = if (currentState.endRepeatOption != "Never" && currentState.repeatDaily) { // Solo si hay una fecha de finalización y se repite
                currentState.endRepeatDate
            } else {
                Instant.now()

            },
            type = 0, // Asumiendo un valor por defecto o que lo obtienes de otra parte del UiState
            repeatAt = currentState.repeatTime, // Directamente desde el UiState
            repeatDaily = currentState.repeatDaily // Directamente desde el UiState
        )

        viewModelScope.launch {
            try {
                addTaskUseCase(taskToSave) // Llama al caso de uso inyectado
                _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
            } catch (e: Exception) {
                // Manejar errores (ej. mostrar un Snackbar, loguear el error)
                _uiState.update { it.copy(isSaving = false, errorMessage = "Error al guardar la tarea: ${e.message}") }
                // Log.e("CreateTaskViewModel", "Error saving task", e)
            }
        }
    }

    // Llama a esta función desde la UI después de que la navegación o el mensaje de éxito se haya manejado
    fun onSaveSuccessConsumed() {
        _uiState.update { it.copy(saveSuccess = false) }
    }

    // Llama a esta función desde la UI para limpiar un mensaje de error
    fun onErrorMessageConsumed() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}