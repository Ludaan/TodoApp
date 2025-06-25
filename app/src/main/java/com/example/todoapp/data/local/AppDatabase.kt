package com.example.todoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.local.entities.TaskEntity

@Database(
    entities = [TaskEntity::class],
    version = 1,
    exportSchema = true
)

abstract class AppDatabase : RoomDatabase(){
    abstract fun taskDao() : TaskDao
}