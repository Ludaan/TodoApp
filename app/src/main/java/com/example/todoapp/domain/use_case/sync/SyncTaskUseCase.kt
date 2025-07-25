package com.example.todoapp.domain.use_case.sync

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.todoapp.core.util.DataState
import com.example.todoapp.domain.logic.sync.SyncManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SyncTasksUseCase @Inject constructor(
    private val syncManager: SyncManager
) {
    @RequiresApi(Build.VERSION_CODES.O)
    operator fun invoke(): Flow<DataState<Unit>> {
        return syncManager.syncReactive()
    }
}