package com.example.todoapp.domain.logic.sync

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.domain.model.Task

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
                localTask.createdAt > remoteTask.createdAt -> localTask
                else -> remoteTask
            }
        }

        return merged.values.toList()
    }
}