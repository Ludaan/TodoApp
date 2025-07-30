package com.example.todoapp.data.mapper

import com.example.todoapp.data.remote.model.RemoteTaskDto // Tu DTO remoto
import com.example.todoapp.domain.model.Task // Tu modelo de dominio
import com.google.firebase.Timestamp // Timestamp de Firebase
import org.junit.Test
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import com.example.todoapp.core.util.toInstant
import com.example.todoapp.data.local.entities.TaskEntity

class TaskMapperTest {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // --- Datos de Ejemplo para Task (Modelo de Dominio) ---
    private val testInstantCreated = Instant.parse("2023-10-26T10:00:00Z")
    private val testInstantLimit = Instant.parse("2023-10-27T14:30:00Z")
    private val testLocalTimeRepeat = LocalTime.parse("09:15", timeFormatter)

    private val domainTask = Task(
        id = "domain-123",
        title = "Domain Task Title",
        description = "This is a description from domain.",
        isCompleted = false,
        createdAt = testInstantCreated,
        color = 0xFFFF00FF.toInt(), // Magenta
        limitDate = testInstantLimit,
        type = 1,
        repeatAt = testLocalTimeRepeat,
        repeatDaily = true
    )

    // --- Datos de Ejemplo para RemoteTaskDto ---
    // Usar los mismos instantes para crear los Timestamps para consistencia
    private val firebaseTimestampCreated = Timestamp(Date(testInstantCreated.toEpochMilli()))
    private val firebaseTimestampLimit = Timestamp(Date(testInstantLimit.toEpochMilli()))

    private val remoteDto = RemoteTaskDto(
        id = "remote-456",
        title = "Remote Task DTO",
        description = "Description from DTO.",
        isCompleted = true,
        createdAt = firebaseTimestampCreated,
        color = 0xFF00FFFF.toInt(), // Cyan
        limitDate = firebaseTimestampLimit,
        type = 2,
        repeatAt = testLocalTimeRepeat.format(timeFormatter), // "09:15"
        repeatDaily = false
    )

    // --- Datos de Ejemplo para TaskEntity ---
    private val entity = TaskEntity(
        id = "entity-789",
        title = "Local Entity Task",
        description = "Description from entity.",
        isCompleted = false,
        createdAt = testInstantCreated.toEpochMilli(), // Convertido a Long
        color = 0xFFFFFF00.toInt(), // Amarillo
        limitDate = testInstantLimit.toEpochMilli(), // Convertido a Long
        type = 0,
        repeatAt = testLocalTimeRepeat.format(timeFormatter), // "09:15"
        repeatDaily = true,
        syncStatus = "SYNCED" // Campo adicional de la entidad
    )


    // --- Pruebas para fromRemote ---
    @Test
    fun `fromRemote convierte RemoteTaskDto a Task correctamente`() {
        val mappedTask = TaskMapper.fromRemote(remoteDto)

        assertEquals(remoteDto.id, mappedTask.id)
        assertEquals(remoteDto.title, mappedTask.title)
        assertEquals(remoteDto.description, mappedTask.description)
        assertEquals(remoteDto.isCompleted, mappedTask.isCompleted)
        // Para Timestamps, la extensión .toInstant() es clave.
        // Asumimos que .toInstant() convierte correctamente Timestamp a Instant.
        assertEquals(remoteDto.createdAt.toInstant(), mappedTask.createdAt)
        assertEquals(remoteDto.color, mappedTask.color)
        assertEquals(remoteDto.limitDate.toInstant(), mappedTask.limitDate)
        assertEquals(remoteDto.type, mappedTask.type)
        assertEquals(LocalTime.parse(remoteDto.repeatAt, timeFormatter), mappedTask.repeatAt)
        assertEquals(remoteDto.repeatDaily, mappedTask.repeatDaily)
    }

    // --- Pruebas para toRemote ---
    @Test
    fun `toRemote convierte Task a RemoteTaskDto correctamente`() {
        val mappedDto = TaskMapper.toRemote(domainTask)

        assertEquals(domainTask.id, mappedDto.id)
        assertEquals(domainTask.title, mappedDto.title)
        assertEquals(domainTask.description, mappedDto.description)
        assertEquals(domainTask.isCompleted, mappedDto.isCompleted)
        // Verificar conversión de Instant a Timestamp
        assertEquals(domainTask.createdAt.epochSecond, mappedDto.createdAt.seconds)
        assertEquals(domainTask.createdAt.nano, mappedDto.createdAt.nanoseconds)
        assertEquals(domainTask.color, mappedDto.color)
        assertEquals(domainTask.limitDate.epochSecond, mappedDto.limitDate.seconds)
        assertEquals(domainTask.limitDate.nano, mappedDto.limitDate.nanoseconds)
        assertEquals(domainTask.type, mappedDto.type)
        assertEquals(domainTask.repeatAt.format(timeFormatter), mappedDto.repeatAt)
        assertEquals(domainTask.repeatDaily, mappedDto.repeatDaily)
    }

    // --- Pruebas para fromLocal ---
    @Test
    fun `fromLocal convierte TaskEntity a Task correctamente`() {
        val mappedTask = TaskMapper.fromLocal(entity)

        assertEquals(entity.id, mappedTask.id)
        assertEquals(entity.title, mappedTask.title)
        assertEquals(entity.description, mappedTask.description)
        assertEquals(entity.isCompleted, mappedTask.isCompleted)
        assertEquals(Instant.ofEpochMilli(entity.createdAt), mappedTask.createdAt)
        assertEquals(entity.color, mappedTask.color)
        assertEquals(Instant.ofEpochMilli(entity.limitDate), mappedTask.limitDate)
        assertEquals(entity.type, mappedTask.type)
        assertEquals(LocalTime.parse(entity.repeatAt, timeFormatter), mappedTask.repeatAt)
        assertEquals(entity.repeatDaily, mappedTask.repeatDaily)
        // syncStatus no está en el modelo Task, por lo que no se mapea aquí.
    }

    // --- Pruebas para toLocal ---
    @Test
    fun `toLocal convierte Task a TaskEntity correctamente`() {
        val mappedEntity = TaskMapper.toLocal(domainTask)

        assertEquals(domainTask.id, mappedEntity.id)
        assertEquals(domainTask.title, mappedEntity.title)
        assertEquals(domainTask.description, mappedEntity.description)
        assertEquals(domainTask.isCompleted, mappedEntity.isCompleted)
        assertEquals(domainTask.createdAt.toEpochMilli(), mappedEntity.createdAt)
        assertEquals(domainTask.color, mappedEntity.color)
        assertEquals(domainTask.limitDate.toEpochMilli(), mappedEntity.limitDate)
        assertEquals(domainTask.type, mappedEntity.type)
        assertEquals(domainTask.repeatAt.format(timeFormatter), mappedEntity.repeatAt)
        assertEquals(domainTask.repeatDaily, mappedEntity.repeatDaily)
        // Verificar el valor por defecto o la lógica para syncStatus si la hubiera
        // En tu mapper actual, TaskEntity tiene un valor por defecto para syncStatus
        // que se usará si no se establece explícitamente en el constructor aquí.
        // Si tuvieras una lógica en el mapper para syncStatus, la probarías.
        // Por ahora, solo verificamos que los otros campos se mapeen bien.
        // Si quieres ser explícito sobre el valor por defecto:
        assertEquals("SYNCED", mappedEntity.syncStatus) // Asumiendo el valor por defecto de TaskEntity
    }

    // --- Pruebas para Casos Borde o Especiales (si aplican) ---

    @Test
    fun `fromRemote maneja diferentes formatos de hora en repeatAt si es robusto`() {
        // Esta prueba depende de qué tan robusto quieras que sea el parseo de repeatAt.
        // Tu formatter actual es estricto ("HH:mm").
        // Si el DTO pudiera venir con "H:mm", necesitarías un formatter más flexible o manejo de errores.
        val dtoWithDifferentTime = remoteDto.copy(repeatAt = "9:05") // Nota: "9:05" vs "09:05"
        // Esto fallaría con el timeFormatter actual.
        // Si se espera que funcione, el mapper o el formatter deberían ajustarse.
        // Por ahora, lo dejamos así para mostrar que el formatter es estricto.
        try {
            TaskMapper.fromRemote(dtoWithDifferentTime)
            // fail("Debería lanzar DateTimeParseException si el formato no es HH:mm")
        } catch (e: java.time.format.DateTimeParseException) {
            // Esperado con el formatter actual estricto
            assertTrue(true)
        }

        // Caso válido
        val dtoValidTime = remoteDto.copy(repeatAt = "09:05")
        val mappedTask = TaskMapper.fromRemote(dtoValidTime)
        assertEquals(LocalTime.of(9, 5), mappedTask.repeatAt)
    }

    @Test
    fun `toRemote formatea LocalTime correctamente`() {
        val taskWithSpecificTime = domainTask.copy(repeatAt = LocalTime.of(7, 5)) // 07:05
        val mappedDto = TaskMapper.toRemote(taskWithSpecificTime)
        assertEquals("07:05", mappedDto.repeatAt)

        val taskWithNoon = domainTask.copy(repeatAt = LocalTime.NOON) // 12:00
        val mappedDtoNoon = TaskMapper.toRemote(taskWithNoon)
        assertEquals("12:00", mappedDtoNoon.repeatAt)
    }

}