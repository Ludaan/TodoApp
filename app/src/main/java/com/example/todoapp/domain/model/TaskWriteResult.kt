package com.example.todoapp.domain.model

sealed class TaskWriteResult {
    data class Synced(val syncedAtEpochMillis: Long = System.currentTimeMillis()) : TaskWriteResult()
    data class PendingSync(
        val localApplied: Boolean = true,
        val message: String? = null
    ) : TaskWriteResult()
}
