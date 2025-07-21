package com.example.todoapp.presentation.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.todoapp.presentation.features.task.CreateTaskScreen
import com.example.todoapp.presentation.features.task.TaskListScreen
import com.example.todoapp.ui.components.bottom_bar.BottomTab

@RequiresApi(Build.VERSION_CODES.O)
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

        composable(route = Screen.CreateTask.route)
        {
            CreateTaskScreen()
        }
    }
}