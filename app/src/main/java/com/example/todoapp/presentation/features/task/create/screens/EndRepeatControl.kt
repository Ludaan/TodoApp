package com.example.todoapp.presentation.features.task.create.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndRepeatControl(
    selectedOption: String, // Expected values: "Never", "OnDate"
    onOptionSelected: (String) -> Unit, // Callback with "Never" or "OnDate"
    currentDateText: String, // Formatted date text from ViewModel
    onDateSelected: (Long?) -> Unit, // Callback for date selection
    modifier: Modifier = Modifier
) {
    // These are the values for the UI display and for the onOptionSelected callback
    val options = listOf(
        EndRepeatOption.NEVER.displayName to EndRepeatOption.NEVER.key,
        EndRepeatOption.ON_DATE.displayName to EndRepeatOption.ON_DATE.key
    )
    var showDatePickerDialog by remember { mutableStateOf(false) }

    // Helper to get the display name for the OutlinedTextField
    val currentDisplayOption = options.find { it.second == selectedOption }?.first ?: EndRepeatOption.NEVER.displayName

    Column(modifier = modifier.fillMaxWidth()) {
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = currentDisplayOption, // Show "Never" or "On a specific date"
                onValueChange = {},
                readOnly = true,
                label = { Text("End repeat") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = outlinedTextFieldColors() // Assuming this is defined
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { (displayName, key) ->
                    DropdownMenuItem(
                        text = { Text(displayName) },
                        onClick = {
                            onOptionSelected(key) // Send back "Never" or "OnDate"
                            expanded = false
                            // If the user selects "On a specific date", show the date picker
                            if (key == EndRepeatOption.ON_DATE.key) {
                                showDatePickerDialog = true
                            }
                        }
                    )
                }
            }
        }

        // Show the selected date and allow changing it if the "OnDate" option is selected
        if (selectedOption == EndRepeatOption.ON_DATE.key) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePickerDialog = true }
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "End date:", // Translated
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentDateText, // This comes formatted from the ViewModel
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // DatePickerDialog, shown if "OnDate" is selected and showDatePickerDialog is true
    if (showDatePickerDialog && selectedOption == EndRepeatOption.ON_DATE.key) {
        val datePickerState = rememberDatePickerState(
            // Optional: Initialize with the current date from uiState if needed
            // initialSelectedDateMillis = uiState.endRepeatDate.toEpochMilli()
            // You would need to pass uiState.endRepeatDate (Instant) here
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateSelected(datePickerState.selectedDateMillis)
                        showDatePickerDialog = false
                    }
                ) { Text("OK") } // Translated
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false }
                ) { Text("CANCEL") } // Translated
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// Optional: Define an enum or sealed class for better type safety and clarity
enum class EndRepeatOption(val key: String, val displayName: String) {
    NEVER("Never", "Never"),
    ON_DATE("OnDate", "On a specific date") // Changed display name for clarity
    }
