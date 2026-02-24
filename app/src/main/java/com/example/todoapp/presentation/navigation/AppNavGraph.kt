package com.example.todoapp.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navOptions
import com.example.todoapp.presentation.features.auth.login.LoginScreen
import com.example.todoapp.presentation.features.auth.profile.ProfileScreen
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
        startDestination = Screen.Login.route,
        modifier = modifier
    ) {
        composable(route = Screen.Login.route) {
            LoginScreen(
                onAuthSuccess = {
                    navController.navigate(
                        Screen.TaskList.route,
                        navOptions {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    )
                }
            )
        }

        composable(route = Screen.TaskList.route) {
            TaskListScreen(
                navController = navController,
                currentRoute = currentRoute,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }

        composable(route = Screen.CreateTask.route) {
            CreateTaskScreen(
                onTaskSavedSuccessfully = {
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                currentRoute = currentRoute,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                onSignedOut = {
                    navController.navigate(
                        Screen.Login.route,
                        navOptions {
                            popUpTo(Screen.TaskList.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    )
                }
            )
        }
    }
}
