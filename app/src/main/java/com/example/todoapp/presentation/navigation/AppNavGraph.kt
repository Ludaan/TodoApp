package com.example.todoapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todoapp.presentation.features.task.TaskListScreen
import com.example.todoapp.ui.components.bottom_bar.BottomTab

@Composable
fun AppNavGraph(navController: NavHostController,
                modifier: Modifier = Modifier,
                onTabChange : (BottomTab) -> Unit = {}
){
    NavHost(navController = navController,
        startDestination = Screen.TaskList.route,
        modifier = modifier
    ){
        composable( route = Screen.TaskList.route){
            onTabChange(BottomTab.Tasks)
            TaskListScreen(modifier)
        }
    }
}