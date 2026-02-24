package com.example.todoapp.data.remote.task

import com.example.todoapp.core.util.DataState
import com.example.todoapp.data.remote.model.RemoteTaskDto
import com.example.todoapp.domain.repository.FirebaseTaskApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseTaskApiImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) :
    FirebaseTaskApi {

    private fun tasksCollection(uid: String) =
        firestore.collection("users").document(uid).collection("tasks")

    override fun getTasks(): Flow<DataState<List<RemoteTaskDto>>> = callbackFlow {
        trySend(DataState.Loading)
        var snapshotListener: ListenerRegistration? = null

        val authListener = FirebaseAuth.AuthStateListener { auth ->
            snapshotListener?.remove()
            snapshotListener = null

            val uid = auth.currentUser?.uid
            if (uid == null) {
                trySend(DataState.Error("No authenticated user"))
                return@AuthStateListener
            }

            snapshotListener = tasksCollection(uid).addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(DataState.Error(error.localizedMessage ?: "Error getting tasks"))
                    return@addSnapshotListener
                }

                val tasks = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toObject(RemoteTaskDto::class.java) }
                trySend(DataState.Success(tasks))
            }
        }

        firebaseAuth.addAuthStateListener(authListener)
        awaitClose {
            snapshotListener?.remove()
            firebaseAuth.removeAuthStateListener(authListener)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun addOrUpdateTask(task: RemoteTaskDto) {
        val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")
        tasksCollection(uid).document(task.id).set(task.copy(userId = uid)).await()
    }

    override suspend fun deleteTask(id: String) {
        val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")
        tasksCollection(uid).document(id).delete().await()
    }

    override suspend fun updateTaskCompletionStatus(
        id: String,
        isCompleted: Boolean,
        updatedAtEpochMillis: Long
    ) {
        val uid = firebaseAuth.currentUser?.uid ?: throw IllegalStateException("No authenticated user")
        val timestamp = Timestamp(updatedAtEpochMillis / 1_000, ((updatedAtEpochMillis % 1_000) * 1_000_000).toInt())
        tasksCollection(uid).document(id).update(
            mapOf(
                "isCompleted" to isCompleted,
                "updatedAt" to timestamp,
                "syncStatus" to "SYNCED"
            )
        ).await()
    }
}
