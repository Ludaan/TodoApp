package com.example.todoapp.presentation.features.auth.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todoapp.core.util.DataState
import com.example.todoapp.ui.components.bottom_bar.BottomBar
import com.example.todoapp.ui.components.bottom_bar.BottomTab

@Composable
fun ProfileScreen(
    currentRoute: String?,
    selectedTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit,
    onSignedOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.userMessageShown()
        }
    }

    LaunchedEffect(uiState.signOutState) {
        if (uiState.signOutState is DataState.Success) {
            onSignedOut()
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != null) {
                BottomBar(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Profile", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = uiState.currentUser?.email ?: "No email",
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyLarge
            )

            OutlinedTextField(
                value = uiState.displayNameInput,
                onValueChange = viewModel::onDisplayNameChanged,
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            Button(
                onClick = viewModel::updateUserProfile,
                enabled = !uiState.isUpdatingProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                if (uiState.isUpdatingProfile) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Update profile")
                }
            }

            Button(
                onClick = viewModel::signOut,
                enabled = !uiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp)
                } else {
                    Text("Sign out")
                }
            }
        }
    }
}
