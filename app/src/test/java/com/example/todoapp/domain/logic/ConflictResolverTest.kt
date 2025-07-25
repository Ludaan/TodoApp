package com.example.todoapp.domain.logic

import com.example.todoapp.domain.logic.sync.ConflictResolver
import com.example.todoapp.domain.model.Task
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalTime

class ConflictResolverTest {

    private lateinit var conflictResolver: ConflictResolver

    // Datos de prueba comunes
    private val baseTime = Instant.parse("2023-01-01T10:00:00Z")
    private val futureTime = baseTime.plusSeconds(3600) // Una hora después para limitDate
    private val noonTime = LocalTime.NOON

    // --- Tareas de ejemplo corregidas según tu modelo Task ---
    private val task1LocalNewer = Task(
        id = "1",
        title = "Local Task 1 (Newer)",
        description = "Description for local task 1",
        isCompleted = false,
        createdAt = baseTime.plusSeconds(100), // Más reciente
        color = 1,
        limitDate = futureTime,
        type = 0,
        repeatAt = noonTime,
        repeatDaily = false
    )
    private val task1RemoteOlder = Task(
        id = "1",
        title = "Remote Task 1 (Older)",
        description = "Description for remote task 1",
        isCompleted = true, // Difiere en algo para que no sean idénticas si createdAt es igual
        createdAt = baseTime, // Más antiguo
        color = 1,
        limitDate = futureTime,
        type = 0,
        repeatAt = noonTime,
        repeatDaily = false
    )

    private val task2RemoteNewer = Task(
        id = "2",
        title = "Remote Task 2 (Newer)",
        description = "Description for remote task 2",
        isCompleted = false,
        createdAt = baseTime.plusSeconds(100), // Más reciente
        color = 2,
        limitDate = futureTime.plusSeconds(100),
        type = 1,
        repeatAt = noonTime.plusHours(1),
        repeatDaily = true
    )
    private val task2LocalOlder = Task(
        id = "2",
        title = "Local Task 2 (Older)",
        description = "Description for local task 2",
        isCompleted = false,
        createdAt = baseTime, // Más antiguo
        color = 2,
        limitDate = futureTime.plusSeconds(100),
        type = 1,
        repeatAt = noonTime.plusHours(1),
        repeatDaily = true
    )

    private val task3OnlyLocal = Task(
        id = "3",
        title = "Only Local Task 3",
        description = "This task only exists locally",
        isCompleted = true,
        createdAt = baseTime,
        color = 3,
        limitDate = futureTime,
        type = 0,
        repeatAt = LocalTime.MIDNIGHT,
        repeatDaily = false
    )

    private val task4OnlyRemote = Task(
        id = "4",
        title = "Only Remote Task 4",
        description = "This task only exists remotely",
        isCompleted = false,
        createdAt = baseTime,
        color = 4,
        limitDate = futureTime.plusSeconds(200),
        type = 2,
        repeatAt = LocalTime.of(15, 30),
        repeatDaily = true
    )

    private val task5SameTimestamp = Task(
        id = "5",
        title = "Local Task 5 (Same Time)",
        description = "Local version for same timestamp conflict",
        isCompleted = false, // Local es 'false'
        createdAt = baseTime,
        color = 5,
        limitDate = futureTime,
        type = 1,
        repeatAt = noonTime,
        repeatDaily = false
    )
    // Para que la regla "else -> remoteTask" tenga efecto cuando createdAt es igual,
    // es bueno que remoteTask sea diferente en algún otro aspecto.
    private val task5SameTimestampRemote = Task(
        id = "5",
        title = "Remote Task 5 (Same Time)",
        description = "Remote version for same timestamp conflict",
        isCompleted = true, // Remote es 'true' (para diferenciar del local si createdAt es igual)
        createdAt = baseTime,
        color = 5, // Mismo color
        limitDate = futureTime,
        type = 1,
        repeatAt = noonTime,
        repeatDaily = false // Podrías cambiar otro campo si tu lógica de "else" es más sofisticada
    )

    @Before
    fun setUp(){
        conflictResolver = ConflictResolver()
    }

    @Test
    fun `resolve cuando tarea local es mas nueva que remota deberia retornar local`(){
        val localTasks = listOf(task1LocalNewer)
        val remoteTasks = listOf(task1RemoteOlder)

        val result = conflictResolver.resolve(localTasks,remoteTasks)

        assertEquals(1, result.size)
        assertEquals(task1LocalNewer, result[0])
    }

    @Test
    fun `resolve cuando tarea remota es mas nueva que local deberia retornar remota`() {
        val localTasks = listOf(task2LocalOlder)
        val remoteTasks = listOf(task2RemoteNewer)

        val result = conflictResolver.resolve(localTasks, remoteTasks)

        assertEquals(1, result.size)
        assertEquals(task2RemoteNewer, result[0])
    }

    @Test
    fun `resolve cuando tarea solo existe localmente deberia retornar local`() {
        val localTasks = listOf(task3OnlyLocal)
        val remoteTasks = emptyList<Task>()

        val result = conflictResolver.resolve(localTasks, remoteTasks)

        assertEquals(1, result.size)
        assertEquals(task3OnlyLocal, result[0])
    }

    @Test
    fun `resolve cuando tarea solo existe remotamente deberia retornar remota`() {
        val localTasks = emptyList<Task>()
        val remoteTasks = listOf(task4OnlyRemote)

        val result = conflictResolver.resolve(localTasks, remoteTasks)

        assertEquals(1, result.size)
        assertEquals(task4OnlyRemote, result[0])
    }

    @Test
    fun `resolve cuando createdAt es identico deberia retornar remota (segun logica actual)`() {
        // La lógica del ConflictResolver que proporcionaste:
        // merged[id] = when {
        //    localTask == null -> remoteTask!!
        //    remoteTask == null -> localTask
        //    localTask.createdAt > remoteTask.createdAt -> localTask
        //    else -> remoteTask // Se elige remote si remoteTask.createdAt >= localTask.createdAt
        // }
        val localTasks = listOf(task5SameTimestamp)
        val remoteTasks = listOf(task5SameTimestampRemote)

        val result = conflictResolver.resolve(localTasks, remoteTasks)

        assertEquals(1, result.size)
        // Dado que remoteTask.createdAt == localTask.createdAt, el 'else' se activa
        assertEquals(task5SameTimestampRemote, result[0])
    }

    @Test
    fun `resolve con listas vacias deberia retornar lista vacia`() {
        val localTasks = emptyList<Task>()
        val remoteTasks = emptyList<Task>()

        val result = conflictResolver.resolve(localTasks, remoteTasks)

        Assert.assertTrue(result.isEmpty())
    }

    @Test
    fun `resolve con multiples tareas y diferentes escenarios`() {
        val localTasks = listOf(task1LocalNewer, task2LocalOlder, task3OnlyLocal, task5SameTimestamp)
        val remoteTasks = listOf(task1RemoteOlder, task2RemoteNewer, task4OnlyRemote, task5SameTimestampRemote)

        // Resultados esperados basados en la lógica de conflicto:
        // task1LocalNewer (local es más nuevo)
        // task2RemoteNewer (remoto es más nuevo)
        // task3OnlyLocal (solo existe localmente)
        // task4OnlyRemote (solo existe remotamente)
        // task5SameTimestampRemote (createdAt es igual, se prefiere remoto)
        val expectedTasks = listOf(
            task1LocalNewer,
            task2RemoteNewer,
            task3OnlyLocal,
            task4OnlyRemote,
            task5SameTimestampRemote
        )
        val expectedIds = expectedTasks.map { it.id }.toSet()

        val result = conflictResolver.resolve(localTasks, remoteTasks)

        assertEquals(expectedTasks.size, result.size)
        assertEquals(expectedIds, result.map { it.id }.toSet()) // Verifica que todos los IDs esperados estén

        // Verifica que cada tarea esperada esté en el resultado.
        // Usar un Set para la comparación es más robusto si el orden no importa.
        assertEquals(expectedTasks.toSet(), result.toSet())
    }

}