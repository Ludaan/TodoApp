package com.example.todoapp.data.remote.model

import com.google.firebase.Timestamp

data class RemoteTaskDto(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val color: Int = 0,
    val limitDate: Timestamp = Timestamp.now(),
    val type: Int = 0,
    val repeatAt: String = "09:00", //Formato HH:mm
    val repeatDaily: Boolean = false,
    val syncStatus: String = "SYNCED"
)
