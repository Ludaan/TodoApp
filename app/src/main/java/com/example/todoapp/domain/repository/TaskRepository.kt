package com.example.todoapp.domain.repository

import com.example.todoapp.data.local.entities.TaskEntity

class TaskRepository {

    abstract fun getTask() : TaskEntity
}