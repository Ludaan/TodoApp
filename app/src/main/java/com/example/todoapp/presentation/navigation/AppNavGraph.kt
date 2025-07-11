package com.example.todoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todoapp.presentation.features.task.TaskListScreen

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier){
    NavHost(navController = navController, startDestination = Screen.TaskList.route){
        composable( route = Screen.TaskList.route){
            TaskListScreen(modifier)
        }
    }
}