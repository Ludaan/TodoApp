package com.example.todoapp.domain.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.mapper.TaskMapper
import com.example.todoapp.data.remote.api.FirebaseTaskApi
import com.example.todoapp.domain.model.Task
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val firebaseApi: FirebaseTaskApi
) : TaskRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getLocalTasks(): List<Task> {
        return taskDao.getAllTasks().map { TaskMapper.fromLocal(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getRemoteTasks(): List<Task> {
        return firebaseApi.getTasks().map { TaskMapper.fromRemote(it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun addLocalTask(task: Task) {
        taskDao.insertTask(TaskMapper.toLocal(task))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun addRemoteTask(task: Task) {
        firebaseApi.addOrUpdateTask(TaskMapper.toRemote(task))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateLocalTasks(tasks: List<Task>) {
        taskDao.insertTasks(tasks.map { TaskMapper.toLocal(it) })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun updateRemoteTasks(tasks: List<Task>) {
        tasks.forEach { firebaseApi.addOrUpdateTask(TaskMapper.toRemote(it)) }
    }

    override suspend fun deleteLocalTask(id: String) {
        taskDao.deleteTaskById(id)
    }

    override suspend fun deleteRemoteTask(id: String) {
        firebaseApi.deleteTask(id)
    }
}