package com.example.todoapp.presentation.features.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todoapp.core.util.DataState

@Composable
fun LoginScreen(
    onAuthSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.authState) {
        if (uiState.authState is DataState.Success) {
            onAuthSuccess()
            viewModel.resetAuthState()
        }
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (uiState.isRegistrationMode) "Create account" else "Sign in",
            style = MaterialTheme.typography.headlineMedium
        )

        OutlinedTextField(
            value = uiState.emailInput,
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )

        OutlinedTextField(
            value = uiState.passwordInput,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )

        if (uiState.isRegistrationMode) {
            OutlinedTextField(
                value = uiState.usernameInput,
                onValueChange = viewModel::onUsernameChanged,
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }

        Button(
            onClick = viewModel::submit,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(2.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (uiState.isRegistrationMode) "Create account" else "Sign in")
            }
        }

        TextButton(
            onClick = { viewModel.toggleRegistrationMode(!uiState.isRegistrationMode) },
            enabled = !uiState.isLoading,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text(
                if (uiState.isRegistrationMode) {
                    "Already have an account? Sign in"
                } else {
                    "No account yet? Create one"
                }
            )
        }

        if (!uiState.isRegistrationMode) {
            TextButton(
                onClick = viewModel::sendPasswordResetEmail,
                enabled = !uiState.isLoading
            ) {
                Text("Forgot password?")
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}
