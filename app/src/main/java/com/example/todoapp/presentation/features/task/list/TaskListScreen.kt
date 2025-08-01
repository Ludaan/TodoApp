package com.example.todoapp.presentation.features.task.list

import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.model.Task
import com.example.todoapp.presentation.features.task.list.viewmodel.TaskListUiState
import com.example.todoapp.presentation.features.task.list.viewmodel.TaskViewModel
import com.example.todoapp.presentation.navigation.Screen
import com.example.todoapp.ui.components.bottom_bar.BottomBar
import com.example.todoapp.ui.components.bottom_bar.BottomTab
import com.example.todoapp.ui.theme.AppTypography
import com.example.todoapp.ui.theme.BorderGray
import com.example.todoapp.ui.theme.PrimaryBlue
import com.example.todoapp.ui.theme.Slate50
import com.example.todoapp.ui.theme.TextPrimary
import com.example.todoapp.ui.theme.TextSecondary
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavHostController,
    currentRoute: String?,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    viewModel: TaskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Estado para el diálogo de confirmación de eliminación
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var taskToDeleteId by remember { mutableStateOf<String?>(null) }

    val syncingComposition by rememberLottieComposition(LottieCompositionSpec.Asset("syncing_lottie.json"))

    // Observar userMessage para mostrar Snackbars
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown() // Limpiar el mensaje después de mostrarlo
        }
    }

    if (showDeleteConfirmationDialog && taskToDeleteId != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                taskToDeleteId?.let { viewModel.deleteTask(it) }
                showDeleteConfirmationDialog = false
                taskToDeleteId = null
            },
            onDismiss = {
                showDeleteConfirmationDialog = false
                taskToDeleteId = null
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected // Llama a la lambda centralizada en MainActivity
            )
        },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Your Tasks",
                        style = AppTypography.headlineLarge.copy(color = TextPrimary)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface, // o primaryContainer
                    titleContentColor = MaterialTheme.colorScheme.onSurface // o onPrimaryContainer
                ),
                actions = {
                    AnimatedVisibility(
                        visible = uiState.syncStatus is DataState.Loading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        LottieAnimation(
                            composition = syncingComposition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier
                                .size(48.dp) // Ajusta según necesites
                                .padding(end = 8.dp)
                        )
                    }
                    AnimatedVisibility(
                        visible = uiState.syncStatus !is DataState.Loading,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        IconButton(onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                viewModel.syncTasks()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Sync requires Android Oreo or higher",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Build,
                                contentDescription = "Sync Tasks",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                // Aquí puedes añadir navigationIcon = { ... } o actions = { ... }
            )
        },
        floatingActionButton = {
            if (currentRoute == Screen.TaskList.route) {
                FloatingActionButton(
                    onClick = {
                        navController.navigate(Screen.CreateTask.route)
                    },
                    containerColor = PrimaryBlue,
                    contentColor = Slate50,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = ""
                    )
                }
            }

        })
    { innerPadding ->
        TaskListContent(
            modifier = Modifier.padding(innerPadding),
            uiState = uiState,
            onTaskCheckChanged = { task ->
                viewModel.toggleTaskCompletion(task)
            },
            onAttemptDeleteTask = { taskId -> // Cambiado para reflejar que es un intento
                taskToDeleteId = taskId
                showDeleteConfirmationDialog = true
            }
        )
    }
}

@Composable
fun TaskListContent(
    modifier: Modifier = Modifier,
    uiState: TaskListUiState,
    onTaskCheckChanged: (task: Task) -> Unit, // Solo la tarea
    onAttemptDeleteTask: (taskId: String) -> Unit
) {
    val loadingTasksComposition by rememberLottieComposition(LottieCompositionSpec.Asset("loading_task_lottie.json"))
    val emptyTasksComposition by rememberLottieComposition(LottieCompositionSpec.Asset("empty_task_lottie.json"))

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = uiState.isLoadingTasks && uiState.tasks.isEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                LottieAnimation(
                    composition = loadingTasksComposition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Loading your tasks...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.tasks.isEmpty() && !uiState.isLoadingTasks,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
            ) {
                LottieAnimation(
                    composition = emptyTasksComposition,
                    iterations = LottieConstants.IterateForever, // o 1 si es una animación más estática
                    modifier = Modifier.size(250.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No tasks yet!\nTap '+' to add a new one.",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(
            // Mostrar la lista solo si hay tareas y no estamos en la fase de carga inicial (donde tasks estaría vacío)
            visible = uiState.tasks.isNotEmpty(), // isLoadingTasks ya no es necesario aquí si confiamos en que tasks se actualiza
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.tasks,
                    key = { task -> task.id }
                ) { task ->
                    // Asegúrate de que este Composable esté disponible y correctamente importado
                    TaskCheckboxItem( // El que estaba en tu código original
                        task = task,
                        checked = task.isCompleted,
                        onCheckedChange = { onTaskCheckChanged(task) },
                        onDeleteRequest = { onAttemptDeleteTask(task.id) }
                    )
                }
            }
        }
    }
}


@Composable
fun TaskCheckboxItem(
    task: Task,
    checked: Boolean, // Este 'checked' viene de task.isCompleted
    onCheckedChange: () -> Unit,
    onDeleteRequest: () -> Unit
) {
    val backgroundColor =
        if (task.color != 0) Color(task.color) else MaterialTheme.colorScheme.surfaceVariant
    val cornerRadius = 8.dp
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                onClick = { onCheckedChange() },
                role = Role.Checkbox
            )
            .background(color = backgroundColor)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(
            checked = checked,
            onCheckedChange = { _ ->
                onCheckedChange()
            },
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryBlue,
                uncheckedColor = BorderGray,
                checkmarkColor = Slate50
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(task.color))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = AppTypography.titleMedium.copy(
                    color = if (checked) TextSecondary else TextPrimary, // Opcional: Cambiar color del texto si está completado
                    textDecoration = if (checked) TextDecoration.LineThrough else null // <--- AQUÍ ESTÁ EL CAMBIO
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // El texto secundario (descripción/hora) usualmente no se tacha,
            // pero podrías aplicarle una lógica similar si lo deseas.
            val secondaryText =
                if (task.description.isNotBlank() && task.description.length < 50) {
                    task.description
                } else {
                    "Repetir a las: ${task.repeatAt.format(DateTimeFormatter.ofPattern("HH:mm"))}"
                }

            Text(
                text = secondaryText,
                style = AppTypography.bodySmall.copy(
                    color = TextSecondary,
                    // Opcional: podrías también aplicar lineThrough aquí si 'checked' es true
                    // textDecoration = if (checked) TextDecoration.LineThrough else null
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones de tarea",
                    tint = TextSecondary
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = {
                        showMenu = false
                        onDeleteRequest()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Eliminar tarea",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar Eliminación") },
        text = { Text("¿Estás seguro de que quieres eliminar esta tarea?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}