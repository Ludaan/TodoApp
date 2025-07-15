package com.example.todoapp.ui.components.bottom_bar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.todoapp.presentation.navigation.Screen

enum class BottomTab(
    val label: String,
    val icon: ImageVector,
    val screen: Screen
) {
    Tasks("Tasks", Icons.AutoMirrored.Filled.List, Screen.TaskList),
    Profile("Profile", Icons.Filled.Person, Screen.Profile)
}
