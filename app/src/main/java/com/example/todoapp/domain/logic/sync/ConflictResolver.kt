package com.example.todoapp.domain.logic.sync

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskSyncStatus

class ConflictResolver {

    @RequiresApi(Build.VERSION_CODES.O)
    fun resolve(
        local: List<Task>,
        remote: List<Task>
    ): List<Task> {
        val merged = mutableMapOf<String, Task>()
        val remoteMap = remote.associateBy { it.id }
        val localMap = local.associateBy { it.id }

        val allIds = (remoteMap.keys + localMap.keys).toSet()

        for (id in allIds) {
            val localTask = localMap[id]
            val remoteTask = remoteMap[id]

            merged[id] = when {
                localTask == null -> remoteTask!!
                remoteTask == null -> localTask
                localTask.updatedAt > remoteTask.updatedAt -> localTask
                remoteTask.updatedAt > localTask.updatedAt -> remoteTask
                localTask.syncStatus == TaskSyncStatus.PENDING_DELETE -> localTask
                else -> remoteTask
            }
        }

        return merged.values.toList()
    }
}
