package com.example.todoapp.presentation.features.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TaskListScreen(modifier: Modifier) {

    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center){


    }

}

@Preview(showSystemUi = true)
@Composable
fun Prev(){
    TaskListScreen(Modifier)
}