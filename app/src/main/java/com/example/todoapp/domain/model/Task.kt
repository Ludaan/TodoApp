package com.example.todoapp.domain.model

import androidx.room.Entity
import java.time.Instant

data class Task(
    val id: String,
    val title: String,
    val isCompleted: Boolean,
    val createdAt: Instant,
    val color: Int,
    val limitDate: Instant,
    val limitHour: Instant,
    val type: Int,
    val repeatAt : Int
)
