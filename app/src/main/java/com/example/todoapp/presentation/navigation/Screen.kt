package com.example.todoapp.presentation.navigation

sealed class Screen(val route: String) {
    data object TaskList : Screen("task_list")
    data object CreateTask : Screen("create_task")
    data object Profile : Screen("create_task")
}