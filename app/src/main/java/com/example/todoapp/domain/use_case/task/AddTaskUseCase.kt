package com.example.todoapp.domain.use_case.task

import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.model.TaskWriteResult
import com.example.todoapp.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(private val repository: TaskRepository) {

    suspend operator fun invoke(task: Task): TaskWriteResult {
        return repository.upsertTask(task)
    }
}
