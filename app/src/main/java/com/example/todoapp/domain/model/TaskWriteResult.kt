package com.example.todoapp.domain.model

sealed class TaskWriteResult {
    data object Synced : TaskWriteResult()
    data class PendingSync(val message: String? = null) : TaskWriteResult()
}
