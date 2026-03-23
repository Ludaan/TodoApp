package com.example.todoapp.data.local.converter

import androidx.room.TypeConverter
import com.example.todoapp.domain.model.TaskSyncStatus

class TaskSyncStatusConverter {

    @TypeConverter
    fun fromTaskSyncStatus(value: TaskSyncStatus): String = value.name

    @TypeConverter
    fun toTaskSyncStatus(value: String): TaskSyncStatus =
        TaskSyncStatus.entries.firstOrNull { it.name == value } ?: TaskSyncStatus.SYNCED
}
