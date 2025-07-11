package com.example.todoapp.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    val color: Int,
    @ColumnInfo(name = "limit_date") val limitDate: Long,
    @ColumnInfo(name = "limit_hour") val limitHour: Long,
    val type: Int,
    @ColumnInfo(name = "repeat_at") val repeatAt: Int,
    @ColumnInfo(name = "sync_status") val syncStatus: String = "SYNCED"
)