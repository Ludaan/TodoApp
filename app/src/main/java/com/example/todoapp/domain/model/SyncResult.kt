package com.example.todoapp.domain.model

data class SyncResult(
    val syncedCount: Int,
    val failedCount: Int,
    val deletedCount: Int,
    val deleteFailedCount: Int,
    val failedTaskIds: List<String> = emptyList(),
    val failedDeleteTaskIds: List<String> = emptyList()
)
