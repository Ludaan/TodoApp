package com.example.todoapp.domain.model

enum class TaskSyncStatus {
    SYNCED,
    PENDING,
    FAILED,
    CONFLICT,
    PENDING_DELETE,
    FAILED_DELETE
}
