package com.example.todoapp.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.core.util.toInstant
import com.example.todoapp.data.local.entities.TaskEntity
import com.example.todoapp.data.remote.model.RemoteTaskDto
import com.example.todoapp.domain.model.Task
import com.google.firebase.Timestamp
import java.time.Instant

object TaskMapper {

    @RequiresApi(Build.VERSION_CODES.O)
    fun fromRemote(dto: RemoteTaskDto): Task = Task(
        id = dto.id,
        title = dto.title,
        isCompleted = dto.isCompleted,
        createdAt = dto.createdAt.toInstant(),
        color = dto.color,
        limitDate = dto.limitDate.toInstant(),
        limitHour = dto.limitHour.toInstant(),
        type = dto.type,
        repeatAt = dto.repeatAt
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun toRemote(task: Task): RemoteTaskDto = RemoteTaskDto(
        id = task.id,
        title = task.title,
        isCompleted = task.isCompleted,
        createdAt = Timestamp(task.createdAt.epochSecond, task.createdAt.nano),
        color = task.color,
        limitDate = Timestamp(task.limitDate.epochSecond, task.limitDate.nano),
        limitHour = Timestamp(task.limitHour.epochSecond, task.limitHour.nano),
        type = task.type,
        repeatAt = task.repeatAt
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun fromLocal(entity: TaskEntity): Task = Task(
        id = entity.id,
        title = entity.title,
        isCompleted = entity.isCompleted,
        createdAt = Instant.ofEpochMilli(entity.createdAt),
        color = entity.color,
        limitDate = Instant.ofEpochMilli(entity.limitDate),
        limitHour = Instant.ofEpochMilli(entity.limitHour),
        type = entity.type,
        repeatAt = entity.repeatAt
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun toLocal(task: Task): TaskEntity = TaskEntity(
        id = task.id,
        title = task.title,
        isCompleted = task.isCompleted,
        createdAt = task.createdAt.toEpochMilli(),
        color = task.color,
        limitDate = task.limitDate.toEpochMilli(),
        limitHour = task.limitHour.toEpochMilli(),
        type = task.type,
        repeatAt = task.repeatAt
    )
}