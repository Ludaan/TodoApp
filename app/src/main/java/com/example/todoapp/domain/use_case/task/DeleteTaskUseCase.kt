package com.example.todoapp.domain.use_case.task

import com.example.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(private val repository: TaskRepository) {

    suspend operator fun invoke(taskId: String){
        repository.deleteRemoteTask(taskId)
        repository.deleteLocalTask(taskId)
    }
}