package com.example.todoapp.domain.model

import java.time.Instant
import java.time.LocalTime

data class Task(
    val id: String,
    val title: String,
    val description : String,
    val isCompleted: Boolean,
    val createdAt: Instant,
    val color: Int,
    val limitDate: Instant,
    val type: Int,
    val repeatAt : LocalTime,
    val repeatDaily : Boolean
)
