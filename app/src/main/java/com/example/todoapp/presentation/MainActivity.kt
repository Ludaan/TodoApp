package com.example.todoapp.presentation

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.todoapp.presentation.navigation.AppNavGraph
import com.example.todoapp.ui.components.bottom_bar.BottomTab
import com.example.todoapp.ui.theme.TodoAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoAppTheme {
                val navController = rememberNavController()
                var selectedTab by remember { mutableStateOf(BottomTab.Tasks) }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                LaunchedEffect(currentRoute) {
                    BottomTab.entries.find { it.screen.route == currentRoute }?.let {
                        if (selectedTab != it) {
                            selectedTab = it
                        }
                    }
                }

                AppNavGraph(
                    navController = navController,
                    modifier = Modifier.fillMaxSize(),
                    currentRoute = currentRoute,
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        if (selectedTab != tab) {
                            selectedTab = tab
                            navController.navigate(tab.screen.route) {
                                // Mantiene una única instancia de cada pantalla de tab en el backstack
                                launchSingleTop = true
                                // Restaura el estado de la pantalla si ya estaba en el backstack
                                restoreState = true
                                // Limpia el backstack hasta el inicio del grafo de navegación
                                // para evitar acumular pantallas al cambiar de tabs.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true // Guarda el estado de las pantallas limpiadas
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}
