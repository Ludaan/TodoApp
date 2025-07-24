package com.example.todoapp.presentation.features.task.create.viewmodel

import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.LocalTime


data class CreateTaskUiState(
    val title: String = "",
    val description: String = "",
    val selectedCategory: String = "Work",
    val isCategoryDropdownExpanded: Boolean = false,
    val categories: List<String> = listOf("Work", "Home", "Study", "Custom"),
    val selectedColor: Color = Color(0xFF0B80EE), // PrimaryBlue
    val colorOptions: List<Color> = listOf(
        Color(0xFF0B80EE), // Azul
        Color(0xFFFFC107), // Amarillo
        Color(0xFFF44336), // Rojo
        Color(0xFF9C27B0), // Púrpura
        Color(0xFF03A9F4)  // Celeste
    ),
    val repeatDaily: Boolean = false,
    val showTimePicker: Boolean = false,
    val endRepeatOption: String = "Never",
    val repeatTime: LocalTime = LocalTime.of(9, 0),
    val endRepeatDate: Instant = Instant.now(),
    val endRepeatDateText: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)