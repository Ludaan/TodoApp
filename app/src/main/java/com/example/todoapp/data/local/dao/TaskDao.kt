package com.example.todoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.todoapp.data.local.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    // Insertar una nueva tarea
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    // Actualizar una tarea existente
    @Update
    suspend fun updateTask(task: TaskEntity): Int

    // Eliminar una tarea
    @Delete
    suspend fun deleteTask(task: TaskEntity): Int

    // Obtener todas las tareas (observable)
    @Query("SELECT * FROM tasks ORDER BY created_at DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    // Obtener tareas por estado (completadas/no completadas)
    @Query("SELECT * FROM tasks WHERE is_completed = :isCompleted")
    fun getTasksByCompletion(isCompleted: Boolean): Flow<List<TaskEntity>>

    // Búsqueda de tareas por título (insensible a mayúsculas)
    @Query("SELECT * FROM tasks WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%'")
    fun searchTasks(query: String): Flow<List<TaskEntity>>

    // Obtener tareas con conflictos de sincronización
    @Query("SELECT * FROM tasks WHERE sync_status = 'CONFLICT'")
    fun getConflictedTasks(): Flow<List<TaskEntity>>

    // Actualizar solo el estado de completado
    @Query("UPDATE tasks SET is_completed = :isCompleted WHERE id = :taskId")
    suspend fun updateCompletionStatus(taskId: String, isCompleted: Boolean): Int

    // Obtener tareas paginadas (para listas grandes)
    //@Query("SELECT * FROM tasks ORDER BY created_at DESC")
    //fun getPagedTasks(): PagingSource<Int, TaskEntity>

    // Transacción: Marcar todas como completadas
    @Transaction
    suspend fun markAllAsCompleted() {
        // Lógica compleja que involucra múltiples operaciones
        getIncompleteTasks().forEach { task ->
            updateCompletionStatus(task.id, true)
        }
    }

    // Consulta para tareas incompletas (usada en la transacción)
    @Query("SELECT * FROM tasks WHERE is_completed = 0")
    suspend fun getIncompleteTasks(): List<TaskEntity>
}
