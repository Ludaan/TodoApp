package com.example.todoapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.presentation.navigation.AppNavGraph
import com.example.todoapp.ui.components.bottom_bar.BottomBar
import com.example.todoapp.ui.components.bottom_bar.BottomTab
import com.example.todoapp.ui.theme.TodoAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoAppTheme {
                val navController = rememberNavController()
                var selectedTab by remember { mutableStateOf(BottomTab.Tasks) }

                Scaffold(modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomBar(
                            selectedTab = selectedTab,
                            onTabSelected = { tab  ->
                                selectedTab = tab
                                navController.navigate(tab.screen.route){
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId){
                                        saveState = true
                                    }
                                }
                            }
                        )
                    })
                { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        modifier =  Modifier.padding(innerPadding),
                        onTabChange = { tab -> selectedTab = tab}
                    )
                }
            }
        }
    }
}
