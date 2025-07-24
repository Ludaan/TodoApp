package com.example.todoapp.presentation.features.task.create.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepeatTimePicker(
    repeatTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePickerDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = repeatTime.hour,
        initialMinute = repeatTime.minute,
        is24Hour = false // Ajusta según tus necesidades o preferencias del usuario
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showTimePickerDialog = true }
            .padding(vertical = 12.dp), // Padding para mejor toque
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Repeat Time",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = repeatTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }

    if (showTimePickerDialog) {
        TimePickerDialog( // Usando un Composable contenedor para el diálogo de tiempo
            onDismiss = { showTimePickerDialog = false },
            onConfirm = {
                val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                onTimeSelected(selectedTime)
                showTimePickerDialog = false
            },
            timePickerState = timePickerState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    title: String = "Select Hour",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    timePickerState: TimePickerState // Asegúrate de que este sea el TimePickerState de M3
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("ACCEPT")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        }
    )
}
