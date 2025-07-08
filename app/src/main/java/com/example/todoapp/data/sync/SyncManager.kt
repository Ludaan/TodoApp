package com.example.todoapp.data.sync

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val repository: TaskRepository,
    private val conflictResolver: ConflictResolver,
    private val ioDispatcher: CoroutineDispatcher
) {

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun sync() = withContext(ioDispatcher) {
        val localTasks = repository.getLocalTasks()
        val remoteTasks = repository.getRemoteTasks()

        val resolvedTasks = conflictResolver.resolve(localTasks, remoteTasks)

        repository.updateLocalTasks(resolvedTasks)
        repository.updateRemoteTasks(resolvedTasks)
    }
}