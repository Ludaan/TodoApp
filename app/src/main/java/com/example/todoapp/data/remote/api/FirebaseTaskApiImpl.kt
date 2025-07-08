package com.example.todoapp.data.remote.api

import com.example.todoapp.data.remote.model.RemoteTaskDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseTaskApiImpl @Inject constructor(private val firestore: FirebaseFirestore) : FirebaseTaskApi {

    private val tasksCollection = firestore.collection("tasks")
    override suspend fun getTasks(): List<RemoteTaskDto> = withContext(Dispatchers.IO) {
        tasksCollection.get().await()
            .documents.mapNotNull { it.toObject(RemoteTaskDto::class.java) }
    }

    override suspend fun addOrUpdateTask(task: RemoteTaskDto) {
        tasksCollection.document(task.id).set(task).await()
    }

    override suspend fun deleteTask(id: String) {
        tasksCollection.document(id).delete().await()
    }
}