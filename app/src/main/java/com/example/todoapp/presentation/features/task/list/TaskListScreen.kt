package com.example.todoapp.presentation.features.task.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    navController: NavHostController,
    currentRoute: String?,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
) {
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
        Column(modifier = Modifier.fillMaxSize()
            .padding(innerPadding)
            , horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = false,
                    onCheckedChange = { },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryBlue,
                        uncheckedColor = BorderGray,
                        checkmarkColor = Slate50
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Grocery Shopping",
                        style = AppTypography.titleMedium.copy(color = TextPrimary),
                        maxLines = 1
                    )
                    Text(
                        text = "11:00 AM",
                        style = AppTypography.bodySmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically
            ) {

                Checkbox(
                    checked = false,
                    onCheckedChange = { },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryBlue,
                        uncheckedColor = BorderGray,
                        checkmarkColor = Slate50
                    )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Grocery Shopping",
                        style = AppTypography.titleMedium.copy(color = TextPrimary),
                        maxLines = 1
                    )
                    Text(
                        text = "11:00 AM",
                        style = AppTypography.bodySmall.copy(color = TextSecondary),
                        maxLines = 1
                    )
                }
            }
        }
    }

}

@Preview(showSystemUi = true)
@Composable
fun Prev() {

}

@Composable
fun TaskCheckboxItem(
    title: String,
    time: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Slate50)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
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

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = AppTypography.titleMedium.copy(color = TextPrimary),
                maxLines = 1
            )
            Text(
                text = time,
                style = AppTypography.bodySmall.copy(color = TextSecondary),
                maxLines = 1
            )
        }
    }
}