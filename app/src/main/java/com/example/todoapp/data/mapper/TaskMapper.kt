package com.example.todoapp.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.core.util.toInstant
import com.example.todoapp.data.local.entities.TaskEntity
import com.example.todoapp.data.remote.model.RemoteTaskDto
import com.example.todoapp.domain.model.Task
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object TaskMapper {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    @RequiresApi(Build.VERSION_CODES.O)
    fun fromRemote(dto: RemoteTaskDto): Task = Task(
        id = dto.id,
        title = dto.title,
        isCompleted = dto.isCompleted,
        createdAt = dto.createdAt.toInstant(),
        color = dto.color,
        limitDate = dto.limitDate.toInstant(),
        type = dto.type,
        repeatAt = LocalTime.parse(dto.repeatAt, timeFormatter),
        description = dto.description,
        repeatDaily = dto.repeatDaily
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun toRemote(task: Task): RemoteTaskDto = RemoteTaskDto(
        id = task.id,
        title = task.title,
        isCompleted = task.isCompleted,
        createdAt = Timestamp(task.createdAt.epochSecond, task.createdAt.nano),
        color = task.color,
        limitDate = Timestamp(task.limitDate.epochSecond, task.limitDate.nano),
        type = task.type,
        repeatAt = task.repeatAt.format(timeFormatter),
        description = task.description,
        repeatDaily = task.repeatDaily
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun fromLocal(entity: TaskEntity): Task = Task(
        id = entity.id,
        title = entity.title,
        isCompleted = entity.isCompleted,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        color = entity.color,
        limitDate = Instant.ofEpochMilli(entity.limitDate),
        type = entity.type,
        repeatAt = LocalTime.parse(entity.repeatAt, timeFormatter),
        description = entity.description,
        repeatDaily = entity.repeatDaily
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun toLocal(task: Task): TaskEntity = TaskEntity(
        id = task.id,
        title = task.title,
        isCompleted = task.isCompleted,
        createdAt = task.createdAt.toEpochMilli(),
        color = task.color,
        limitDate = task.limitDate.toEpochMilli(),
        type = task.type,
        repeatAt = task.repeatAt.format(timeFormatter),
        description = task.description,
        repeatDaily = task.repeatDaily
    )
}