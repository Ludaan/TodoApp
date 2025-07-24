package com.example.todoapp.presentation.features.task.create.screens

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todoapp.presentation.features.task.create.viewmodel.CreateTaskViewModel
import com.example.todoapp.ui.theme.AppTypography
import com.example.todoapp.ui.theme.TextPrimary
import com.example.todoapp.ui.theme.TextSecondary
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateTaskScreen(
    viewModel: CreateTaskViewModel = hiltViewModel(),
    onTaskSavedSuccessfully: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null){
            focusManager.clearFocus()
            snackbarHostState.showSnackbar(
                message = uiState.errorMessage ?: "",
                duration = SnackbarDuration.Short
            )
            viewModel.onErrorMessageConsumed()
        }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess){
            focusManager.clearFocus()
            onTaskSavedSuccessfully()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "New Task",
                        style = AppTypography.headlineLarge.copy(color = TextPrimary),
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = uiState.title,
                onValueChange = { viewModel.onTitleChange(it) },
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
                isError = uiState.errorMessage?.contains("title", ignoreCase = true) == true,
                colors = outlinedTextFieldColors()
            )
            ErrorDisplay(
                errorMessage = uiState.errorMessage,
                fieldIdentifier = "title"
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Descripción
            OutlinedTextField(
                value = uiState.description,
                onValueChange = { viewModel.onDescriptionChange(it) },
                placeholder = { Text("Description", color = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Categoría con ExposedDropdownMenuBox

            CategorySelector(
                selectedCategory = uiState.selectedCategory,
                categories = uiState.categories,
                expanded = uiState.isCategoryDropdownExpanded,
                onExpandedChange = { viewModel.onCategoryDropdownExpandedChange(it) },
                onCategorySelected = { viewModel.onCategorySelected(it) },
                onDismiss = { viewModel.onCategoryDropdownDismiss() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de color

            ColorSelector(
                colorOptions = uiState.colorOptions,
                selectedColor = uiState.selectedColor,
                onColorSelected = { viewModel.onColorSelected(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Repetir diariamente
            SwitchRow(
                text = "Repeat Daily",
                checked = uiState.repeatDaily,
                onCheckedChange = { viewModel.onRepeatDailyChange(it) }
            )

            // Controles de repetición condicionales
            if (uiState.repeatDaily) {
                Spacer(modifier = Modifier.height(12.dp))
                RepeatTimePicker(
                    repeatTime = uiState.repeatTime,
                    onTimeSelected = { viewModel.onRepeatTimeChange(it) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                EndRepeatControl(
                    selectedOption = uiState.endRepeatOption,
                    onOptionSelected = { viewModel.onEndRepeatOptionChange(it) },
                    currentDateText = uiState.endRepeatDateText,
                    onDateSelected = { millis ->
                        millis?.let { viewModel.onEndRepeatDateChange(Instant.ofEpochMilli(it)) }
                    }
                )
            }
            Spacer(modifier = Modifier.weight(1f)) // Empuja el botón hacia abajo

            Button(
                onClick = {
                    focusManager.clearFocus() // Ocultar teclado antes de intentar guardar
                    //viewModel.saveTask()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = MaterialTheme.shapes.medium,
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Text("Save Task", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // Espacio al final
        }
    }

}


@Composable
fun ErrorDisplay(errorMessage: String?, fieldIdentifier: String) {
    if (errorMessage?.contains(fieldIdentifier, ignoreCase = true) == true) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelector(
    selectedCategory: String,
    categories: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onCategorySelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategory,
            onValueChange = {}, // No editable directamente
            readOnly = true,
            label = { Text("Category", style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor() // Importante
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category, style = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary)) },
                    onClick = {
                        onCategorySelected(category)
                        // onExpandedChange(false) // El ViewModel lo maneja a través de onCategorySelected
                    }
                )
            }
        }
    }
}

@Composable
fun ColorSelector(
    colorOptions: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    Column {
        Text(
            "Selecciona un color:",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            colorOptions.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (color == selectedColor) 2.5.dp else 0.dp,
                            color = if (color == selectedColor) MaterialTheme.colorScheme.outline else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
fun SwitchRow(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
            )
        )
    }
}

@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = MaterialTheme.colorScheme.primary,
    // Puedes personalizar más colores si es necesario
    // focusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
    // unfocusedContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
)



