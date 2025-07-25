package com.example.todoapp.domain.logic

import com.example.todoapp.domain.logic.sync.ConflictResolver
import com.example.todoapp.domain.model.Task
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

}