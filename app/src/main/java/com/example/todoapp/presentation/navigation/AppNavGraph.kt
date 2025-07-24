package com.example.todoapp.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todoapp.presentation.features.task.create.screens.CreateTaskScreen
import com.example.todoapp.presentation.features.task.list.TaskListScreen
import com.example.todoapp.ui.components.bottom_bar.BottomTab

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    currentRoute: String?, // Para pasarlo a pantallas que lo necesiten
    selectedTab: BottomTab, // Para pasarlo a pantallas con BottomBar
    onTabSelected: (BottomTab) -> Unit // Para pasarlo a pantallas con BottomBar
) {
    NavHost(
        navController = navController,
        startDestination = Screen.TaskList.route,
        modifier = modifier
    ) {
        composable(route = Screen.TaskList.route) {
            TaskListScreen(
                navController = navController,
                currentRoute = currentRoute,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }

        composable(route = Screen.CreateTask.route)
        {
            CreateTaskScreen(
                onTaskSavedSuccessfully = {
                    navController.popBackStack()
                }
            )
        }
    }
}