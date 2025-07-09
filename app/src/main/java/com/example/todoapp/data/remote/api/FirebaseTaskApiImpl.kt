package com.example.todoapp.data.remote.api

import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.remote.model.RemoteTaskDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseTaskApiImpl @Inject constructor(private val firestore: FirebaseFirestore) : FirebaseTaskApi {

    private val tasksCollection = firestore.collection("tasks")

    override fun getTasks(): Flow<DataState<List<RemoteTaskDto>>> = flow {
        emit(DataState.Loading)
        try {
            val snapshot = tasksCollection.get().await()
            val tasks = snapshot.documents.mapNotNull { it.toObject(RemoteTaskDto::class.java) }

            emit(DataState.Success(tasks))
        }catch (e: Exception){
            emit(DataState.Error(e.localizedMessage ?: "Error getting tasks"))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun addOrUpdateTask(task: RemoteTaskDto) {
        tasksCollection.document(task.id).set(task).await()
    }

    override suspend fun deleteTask(id: String) {
        tasksCollection.document(id).delete().await()
    }
}