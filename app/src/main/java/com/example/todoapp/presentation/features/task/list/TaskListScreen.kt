package com.example.todoapp.presentation.features.task.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.domain.model.Task
import com.example.todoapp.presentation.features.task.list.viewmodel.TaskViewModel
import com.example.todoapp.presentation.navigation.AppNavGraph
import com.example.todoapp.presentation.navigation.Screen
import com.example.todoapp.ui.components.bottom_bar.BottomBar
import com.example.todoapp.ui.components.bottom_bar.BottomTab
import com.example.todoapp.ui.theme.AppTypography
import com.example.todoapp.ui.theme.BorderGray
import com.example.todoapp.ui.theme.PrimaryBlue
import com.example.todoapp.ui.theme.Slate50
import com.example.todoapp.ui.theme.TextPrimary
import com.example.todoapp.ui.theme.TextSecondary
import com.example.todoapp.ui.theme.TodoAppTheme
import java.time.Instant
import java.time.LocalTime
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

    // Observar userMessage para mostrar Snackbars
    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown() // Limpiar el mensaje después de mostrarlo
        }
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
                )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding), horizontalAlignment = Alignment.CenterHorizontally
        ) {


        }
    }
}

@Composable
fun TaskCheckboxItem(
    task: Task,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val backgroundColor = if (task.color != 0) Color(task.color) else MaterialTheme.colorScheme.surfaceVariant
    val cornerRadius = 8.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(
                onClick = { onCheckedChange(!checked) },
                role = Role.Checkbox
            )
            .background(color = backgroundColor)
            .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
    ) {

        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryBlue,
                uncheckedColor = BorderGray,
                checkmarkColor = Slate50
            )
        )

         Box(
             modifier = Modifier
                 .size(16.dp)
                 .clip(CircleShape)
                 .background(Color(task.color))
         )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                style = AppTypography.titleMedium.copy(color = TextPrimary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Mostrar la descripción si existe y no es muy larga, o la hora de repetición
            val secondaryText = if (task.description.isNotBlank() && task.description.length < 50) { // Límite arbitrario
                task.description
            } else {
                "Repetir a las: ${task.repeatAt.format(DateTimeFormatter.ofPattern("HH:mm"))}"
            }

            Text(
                text = secondaryText,
                style = AppTypography.bodySmall.copy(color = TextSecondary),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// --- Previews ---

@Preview(showBackground = true, name = "Task Item - Unchecked")
@Composable
fun TaskCheckboxItemUncheckedPreview() {
    // Puedes envolverlo en tu tema si es necesario para que los colores MaterialTheme funcionen
    // YourAppTheme {
    Surface { // Surface para que showBackground funcione mejor con el color de fondo del item
        TaskCheckboxItem(
            task = Task(
                id = "1",
                title = "Comprar leche y pan para la cena de esta noche",
                description = "Recordar que sea deslactosada",
                isCompleted = false,
                createdAt = Instant.now(),
                color = 0xFFE1BEE7.toInt(), // Lila claro (ejemplo de color ARGB Int)
                limitDate = Instant.now().plusSeconds(3600 * 24),
                type = 1,
                repeatAt = LocalTime.of(9, 0),
                repeatDaily = true
            ),
            checked = false,
            onCheckedChange = {}
        )
    }
    // }
}

@Preview(showBackground = true, name = "Task Item - Checked")
@Composable
fun TaskCheckboxItemCheckedPreview() {
    // YourAppTheme {
    Surface(color = MaterialTheme.colorScheme.background) { // Fondo para ver el contraste del redondeado
        TaskCheckboxItem(
            task = Task(
                id = "2",
                title = "Terminar el informe del proyecto",
                description = "Incluir gráficos y conclusiones",
                isCompleted = true,
                createdAt = Instant.now(),
                color = android.graphics.Color.argb(255, 178, 223, 219), // Verde azulado claro
                limitDate = Instant.now().plusSeconds(3600 * 2),
                type = 0,
                repeatAt = LocalTime.of(15, 30),
                repeatDaily = false
            ),
            checked = true,
            onCheckedChange = {}
        )
    }
    // }
}

@Preview(showBackground = true, name = "Task Item - Sin Color Específico")
@Composable
fun TaskCheckboxItemDefaultColorPreview() {
    // YourAppTheme {
    Surface {
        TaskCheckboxItem(
            task = Task(
                id = "3",
                title = "Llamar al dentista",
                description = "", // Descripción vacía
                isCompleted = false,
                createdAt = Instant.now(),
                color = 0, // Sin color específico o color por defecto
                limitDate = Instant.now().plusSeconds(3600 * 5),
                type = 2,
                repeatAt = LocalTime.of(11, 0),
                repeatDaily = false
            ),
            checked = false,
            onCheckedChange = {}
        )
    }
    // }
}