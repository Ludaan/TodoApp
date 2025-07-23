package com.example.todoapp.presentation.features.task

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todoapp.ui.theme.AppTypography
import com.example.todoapp.ui.theme.InputBackground
import com.example.todoapp.ui.theme.PrimaryBlue
import com.example.todoapp.ui.theme.TextPrimary
import com.example.todoapp.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateTaskScreen() {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Work") }
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Work", "Home", "Study", "Custom")
    var selectedColor by remember { mutableStateOf(Color(0xFF0B80EE)) } // PrimaryBlue
    val colorOptions = listOf(
        Color(0xFF0B80EE), // Azul
        Color(0xFFFFC107), // Amarillo
        Color(0xFFF44336), // Rojo
        Color(0xFF9C27B0), // Púrpura
        Color(0xFF03A9F4)  // Celeste
    )
    var repeatDaily by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val endRepeat by remember { mutableStateOf("Never") }

    var repeatTime by remember { mutableStateOf(LocalTime.of(9, 0)) }

    var endRepeatInstant by remember { mutableStateOf(Instant.now()) }
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "MX")).withZone(ZoneId.systemDefault())
    val endRepeatText = formatter.format(endRepeatInstant)



    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "New Task",
            style = AppTypography.headlineLarge.copy(color = TextPrimary),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = {
                Text(
                    text = "Task title",
                    style = AppTypography.bodySmall.copy(color = TextSecondary)
                )
            },
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = AppTypography.titleMedium.copy(color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                disabledTextColor = TextSecondary,
                focusedLabelColor = PrimaryBlue,
                unfocusedLabelColor = TextSecondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Description", color = TextSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = InputBackground,
                unfocusedContainerColor = InputBackground,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                disabledTextColor = TextSecondary,
                focusedLabelColor = PrimaryBlue,
                unfocusedLabelColor = TextSecondary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown de categorías
        Box {
            OutlinedTextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InputBackground,
                    unfocusedContainerColor = InputBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    disabledTextColor = TextSecondary,
                    focusedLabelColor = PrimaryBlue,
                    unfocusedLabelColor = TextSecondary
                ),
                placeholder = { Text("Category", color = TextSecondary) }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            selectedCategory = category
                            expanded = false
                        }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { expanded = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selector de color
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colorOptions.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (color == selectedColor) 3.dp else 0.dp,
                            color = if (color == selectedColor) Color.Gray else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = color }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Repetir diariamente
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Repeat Daily", style = AppTypography.titleMedium.copy(color = TextPrimary))
            Switch(
                checked = repeatDaily,
                onCheckedChange = { repeatDaily = it }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hora
        RepeatTimePicker(
            repeatTime = repeatTime,
            onTimeSelected = { repeatTime = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Fin de repetición
        EndRepeatRow(
            endRepeat = endRepeatText,
            onDateSelected = { millis ->
                millis?.let {
                    endRepeatInstant = Instant.ofEpochMilli(it)
                }
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Botón de guardar
        Button(
            onClick = { /* TODO: save task */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Save", color = Color.White, style = AppTypography.titleMedium)
        }
    }

    // Time Picker (puedes integrar un dialog personalizado si lo deseas)
    if (showTimePicker) {
        // Mostrar time picker personalizado o usar la librería de tu preferencia
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatTimePicker(
    repeatTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    val timePickerState = rememberTimePickerState(
        initialHour = repeatTime.hour,
        initialMinute = repeatTime.minute,
        is24Hour = false
    )

    // Contenido de UI
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Repeat Time",
            style = AppTypography.titleMedium.copy(color = TextPrimary)
        )
        Text(
            text = repeatTime.format(DateTimeFormatter.ofPattern("h:mm a")),
            style = AppTypography.bodySmall.copy(color = TextSecondary),
            modifier = Modifier.clickable { showTimePicker = true }
        )
    }

    // AlertDialog con TimePicker
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    val selectedTime = LocalTime.of(
                        timePickerState.hour,
                        timePickerState.minute
                    )
                    onTimeSelected(selectedTime)
                }) {
                    Text("OK", color = PrimaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            title = {
                Text("Select Time", style = AppTypography.titleMedium)
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndRepeatRow(
    endRepeat: String,
    onDateSelected: (Long?) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDatePicker = false
                }) {
                    Text("OK", style = AppTypography.bodySmall.copy(color = PrimaryBlue))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", style = AppTypography.bodySmall.copy(color = TextSecondary))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "End Repeat",
            style = AppTypography.titleMedium.copy(color = TextPrimary)
        )
        Text(
            text = endRepeat,
            style = AppTypography.bodySmall.copy(color = TextSecondary),
            modifier = Modifier.clickable { showDatePicker = true }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showSystemUi = true)
@Composable
fun Prevv() {
    CreateTaskScreen()
}


