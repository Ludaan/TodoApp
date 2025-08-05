package com.example.todoapp.data.repository

import app.cash.turbine.test
import com.example.todoapp.core.util.DataState // Tu clase DataState
import com.example.todoapp.data.local.dao.TaskDao
import com.example.todoapp.data.local.entities.TaskEntity
import com.example.todoapp.data.mapper.TaskMapper // Tu mapper
import com.example.todoapp.domain.repository.FirebaseTaskApi
import com.example.todoapp.data.remote.model.RemoteTaskDto // Tu DTO remoto
import com.example.todoapp.domain.model.Task // Tu modelo de dominio
import com.google.firebase.Timestamp
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.util.Date

@ExperimentalCoroutinesApi
class TaskRepositoryImplTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockTaskDao: TaskDao

    @RelaxedMockK
    private lateinit var mockFirebaseApi: FirebaseTaskApi

    private lateinit var repository: TaskRepositoryImpl

    // --- Datos de ejemplo actualizados para TaskEntity (sin cambios aquí) ---
    private val currentTimeMillis = System.currentTimeMillis()
    private val oneHourAgoMillis = currentTimeMillis - 3600 * 1000

    private val taskEntity1 = TaskEntity(
        id = "id1", title = "Local Task 1", description = "Desc1", isCompleted = false,
        createdAt = currentTimeMillis, color = 0xFFFF0000.toInt(),
        limitDate = currentTimeMillis + 86400 * 1000, type = 1,
        repeatAt = "10:00", repeatDaily = true, syncStatus = "SYNCED"
    )
    private val taskEntity2 = TaskEntity(
        id = "id2", title = "Local Task 2", description = "Desc2", isCompleted = true,
        createdAt = oneHourAgoMillis, color = 0xFF00FF00.toInt(),
        limitDate = oneHourAgoMillis + 86400 * 1000 * 2, type = 0,
        repeatAt = "14:30", repeatDaily = false, syncStatus = "PENDING"
    )
    private val localTaskEntities = listOf(taskEntity1, taskEntity2)


    // --- RemoteTaskDto AHORA USA Firebase Timestamp ---
    private val firebaseNow = Timestamp.now()
    private val firebaseOneHourFromNow = Timestamp(Date(System.currentTimeMillis() + 3600 * 1000))
    private val firebaseTwoHoursFromNow = Timestamp(Date(System.currentTimeMillis() + 7200 * 1000))
    private val firebaseThreeHoursFromNow = Timestamp(
        Date(System.currentTimeMillis() + 10800 * 1000)
    )


    private val remoteTaskDto1 = RemoteTaskDto(
        id = "id3", title = "Remote Task 1", description = "Desc3", isCompleted = false,
        createdAt = firebaseNow, // Usando Firebase Timestamp
        color = 0xFF0000FF.toInt(), // Azul
        limitDate = firebaseOneHourFromNow, // Usando Firebase Timestamp
        type = 2, repeatAt = "08:00", repeatDaily = true
    )
    private val remoteTaskDto2 = RemoteTaskDto(
        id = "id4", title = "Remote Task 2", description = "Desc4", isCompleted = true,
        createdAt = Timestamp(Date(System.currentTimeMillis() - 3600 * 1000L * 2)), // Hace dos horas
        color = 0xFFFFFF00.toInt(), // Amarillo
        limitDate = firebaseTwoHoursFromNow, // Usando Firebase Timestamp
        type = 1, repeatAt = "18:00", repeatDaily = false
    )
    private val remoteTaskDtos = listOf(remoteTaskDto1, remoteTaskDto2)


    // --- Domain Tasks (asumiendo que Task usa Instant y LocalTime) ---
    // El mapeo de Timestamp a Instant es crucial aquí
    private val domainTaskFromLocal1 = Task(
        id = "id1", title = "Local Task 1", description = "Desc1", isCompleted = false,
        createdAt = Instant.ofEpochMilli(taskEntity1.createdAt), color = taskEntity1.color,
        limitDate = Instant.ofEpochMilli(taskEntity1.limitDate), type = taskEntity1.type,
        repeatAt = LocalTime.parse(taskEntity1.repeatAt), repeatDaily = taskEntity1.repeatDaily
    )
    private val domainTaskFromLocal2 = Task(
        id = "id2", title = "Local Task 2", description = "Desc2", isCompleted = true,
        createdAt = Instant.ofEpochMilli(taskEntity2.createdAt), color = taskEntity2.color,
        limitDate = Instant.ofEpochMilli(taskEntity2.limitDate), type = taskEntity2.type,
        repeatAt = LocalTime.parse(taskEntity2.repeatAt), repeatDaily = taskEntity2.repeatDaily
    )

    // Ajustar para que coincida con la conversión de Firebase Timestamp
    private val domainTaskFromRemote1 = Task(
        id = "id3", title = "Remote Task 1", description = "Desc3", isCompleted = false,
        createdAt = Instant.ofEpochSecond(remoteTaskDto1.createdAt.seconds, remoteTaskDto1.createdAt.nanoseconds.toLong()),
        color = remoteTaskDto1.color,
        limitDate = Instant.ofEpochSecond(remoteTaskDto1.limitDate.seconds, remoteTaskDto1.limitDate.nanoseconds.toLong()),
        type = remoteTaskDto1.type,
        repeatAt = LocalTime.parse(remoteTaskDto1.repeatAt),
        repeatDaily = remoteTaskDto1.repeatDaily
    )
    private val domainTaskFromRemote2 = Task( // Asegúrate de definir este completamente
        id = "id4", title = "Remote Task 2", description = "Desc4", isCompleted = true,
        createdAt = Instant.ofEpochSecond(remoteTaskDto2.createdAt.seconds, remoteTaskDto2.createdAt.nanoseconds.toLong()),
        color = remoteTaskDto2.color,
        limitDate = Instant.ofEpochSecond(remoteTaskDto2.limitDate.seconds, remoteTaskDto2.limitDate.nanoseconds.toLong()),
        type = remoteTaskDto2.type,
        repeatAt = LocalTime.parse(remoteTaskDto2.repeatAt),
        repeatDaily = remoteTaskDto2.repeatDaily
    )

    private val domainTaskToSave = Task(
        id = "save1", title = "Task to Save", description = "Saving this task", isCompleted = false,
        createdAt = Instant.now(), color = 0xFFAABBCC.toInt(),
        limitDate = Instant.now().plusSeconds(86400), type = 0,
        repeatAt = LocalTime.NOON, repeatDaily = true
    )


    @Before
    fun setUp() {
        repository = TaskRepositoryImpl(mockTaskDao, mockFirebaseApi)

        // Mockear el comportamiento del Mapper estático
        // Es crucial que estos mocks reflejen lo que el mapper real haría.
        // Si TaskMapper no fuera un objeto, lo mockearías como una dependencia normal.
        // Dado que es un objeto con métodos estáticos, usamos mockkObject y every.
        mockkObject(TaskMapper)
        every { TaskMapper.fromLocal(taskEntity1) } returns domainTaskFromLocal1
        every { TaskMapper.fromLocal(taskEntity2) } returns domainTaskFromLocal2
        every { TaskMapper.fromRemote(remoteTaskDto1) } returns domainTaskFromRemote1
        every { TaskMapper.fromRemote(remoteTaskDto2) } returns domainTaskFromRemote2

        // Y para las operaciones inversas
        every { TaskMapper.toLocal(domainTaskFromLocal1) } returns taskEntity1
        every { TaskMapper.toRemote(domainTaskFromRemote1) } returns remoteTaskDto1
    }

    @After
    fun tearDown() {
        unmockkObject(TaskMapper) // Limpiar el mock del objeto
    }

    @Test
    fun `getLocalTasks devuelve Flow de tareas de dominio mapeadas desde DAO`() = runTest {
        // Arrange
        every { mockTaskDao.getAllTasksFlow() } returns flowOf(localTaskEntities)

        // Act
        val resultFlow = repository.getLocalTasks()

        // Assert
        resultFlow.test {
            val emittedList = awaitItem()
            assertEquals(2, emittedList.size)
            assertTrue(emittedList.any { it.id == "id1" && it.title == "Local Task 1" })
            assertTrue(emittedList.any { it.id == "id2" && it.title == "Local Task 2" })
            awaitComplete()
        }
        verify(exactly = 1) { mockTaskDao.getAllTasksFlow() }
        verify(exactly = 1) { TaskMapper.fromLocal(taskEntity1) }
        verify(exactly = 1) { TaskMapper.fromLocal(taskEntity2) }
    }

    @Test
    fun `getRemoteTasks devuelve Flow de DataState con tareas de dominio mapeadas desde API`() = runTest {
        // Arrange
        val successStateFromApi = DataState.Success(remoteTaskDtos)
        every { mockFirebaseApi.getTasks() } returns flowOf(DataState.Loading, successStateFromApi)

        // Act
        val resultFlow = repository.getRemoteTasks()

        // Assert
        resultFlow.test {
            assertEquals(DataState.Loading, awaitItem()) // Espera Loading

            val successState = awaitItem() // Espera Success
            assertTrue(successState is DataState.Success)
            val data = (successState as DataState.Success).data
            assertEquals(2, data.size)
            assertTrue(data.any { it.id == "id3" && it.title == "Remote Task 1" })
            assertTrue(data.any { it.id == "id4" && it.title == "Remote Task 2" })

            awaitComplete()
        }
        verify(exactly = 1) { mockFirebaseApi.getTasks() }
        verify(exactly = 1) { TaskMapper.fromRemote(remoteTaskDto1) }
        verify(exactly = 1) { TaskMapper.fromRemote(remoteTaskDto2) }
    }

    @Test
    fun `getRemoteTasks propaga DataState Error desde API`() = runTest {
        // Arrange
        val errorMessage = "Error de red simulado"
        val errorStateFromApi = DataState.Error(errorMessage) // El tipo genérico es importante
        every { mockFirebaseApi.getTasks() } returns flowOf(DataState.Loading, errorStateFromApi)

        // Act
        val resultFlow = repository.getRemoteTasks()

        // Assert
        resultFlow.test {
            assertEquals(DataState.Loading, awaitItem())

            val errorState = awaitItem()
            assertTrue(errorState is DataState.Error)
            assertEquals(errorMessage, (errorState as DataState.Error).message)

            awaitComplete()
        }
        verify(exactly = 1) { mockFirebaseApi.getTasks() }
        verify(exactly = 0) { TaskMapper.fromRemote(any()) } // El mapper no debería ser llamado en caso de error
    }


    @Test
    fun `addLocalTask llama a DAO con tarea mapeada`() = runTest {
        // Arrange
        val domainTask = domainTaskFromLocal1 // Usamos una tarea de dominio ya mockeada para el mapeo
        val expectedEntity = taskEntity1      // El resultado esperado del TaskMapper.toLocal

        // Asegurarse de que el mock del mapper está configurado para esta tarea específica
        every { TaskMapper.toLocal(domainTask) } returns expectedEntity
        coEvery { mockTaskDao.insertTask(expectedEntity) } returns 1L // insertTask devuelve Long

        // Act
        repository.addLocalTask(domainTask)

        // Assert
        coVerify(exactly = 1) { TaskMapper.toLocal(domainTask) }
        coVerify(exactly = 1) { mockTaskDao.insertTask(expectedEntity) }
    }

    @Test
    fun `addRemoteTask llama a API con tarea mapeada`() = runTest {
        // Arrange
        val domainTask = domainTaskFromRemote1
        val expectedDto = remoteTaskDto1

        every { TaskMapper.toRemote(domainTask) } returns expectedDto
        coEvery { mockFirebaseApi.addOrUpdateTask(expectedDto) } just Runs // addOrUpdateTask es suspend fun Unit

        // Act
        repository.addRemoteTask(domainTask)

        // Assert
        coVerify(exactly = 1) { TaskMapper.toRemote(domainTask) }
        coVerify(exactly = 1) { mockFirebaseApi.addOrUpdateTask(expectedDto) }
    }

    @Test
    fun `updateLocalTasks llama a DAO con lista de tareas mapeadas`() = runTest {
        // Arrange
        val domainTasks = listOf(domainTaskFromLocal1, domainTaskFromLocal2)
        val expectedEntities = listOf(taskEntity1, taskEntity2)

        // Configurar el mock del mapper para cada tarea
        every { TaskMapper.toLocal(domainTaskFromLocal1) } returns taskEntity1
        every { TaskMapper.toLocal(domainTaskFromLocal2) } returns taskEntity2
        coEvery { mockTaskDao.insertTasks(expectedEntities) } returns listOf(1L, 2L)

        // Act
        repository.updateLocalTasks(domainTasks)

        // Assert
        coVerify(exactly = 1) { TaskMapper.toLocal(domainTaskFromLocal1) }
        coVerify(exactly = 1) { TaskMapper.toLocal(domainTaskFromLocal2) }
        coVerify(exactly = 1) { mockTaskDao.insertTasks(expectedEntities) }
    }

    @Test
    fun `updateRemoteTasks llama a API para cada tarea mapeada`() = runTest {
        // Arrange
        val domainTasks = listOf(domainTaskFromRemote1, domainTaskFromRemote2)
        val dto1 = remoteTaskDto1
        val dto2 = remoteTaskDto2

        every { TaskMapper.toRemote(domainTaskFromRemote1) } returns dto1
        every { TaskMapper.toRemote(domainTaskFromRemote2) } returns dto2
        coEvery { mockFirebaseApi.addOrUpdateTask(dto1) } just Runs
        coEvery { mockFirebaseApi.addOrUpdateTask(dto2) } just Runs

        // Act
        repository.updateRemoteTasks(domainTasks)

        // Assert
        coVerify(exactly = 1) { TaskMapper.toRemote(domainTaskFromRemote1) }
        coVerify(exactly = 1) { TaskMapper.toRemote(domainTaskFromRemote2) }
        coVerify(exactly = 1) { mockFirebaseApi.addOrUpdateTask(dto1) }
        coVerify(exactly = 1) { mockFirebaseApi.addOrUpdateTask(dto2) }
    }

    @Test
    fun `deleteLocalTask llama a DAO con id`() = runTest {
        // Arrange
        val taskId = "id_a_borrar"
        coEvery { mockTaskDao.deleteTaskById(taskId) } just Runs

        // Act
        repository.deleteLocalTask(taskId)

        // Assert
        coVerify(exactly = 1) { mockTaskDao.deleteTaskById(taskId) }
    }

    @Test
    fun `deleteRemoteTask llama a API con id`() = runTest {
        // Arrange
        val taskId = "id_a_borrar_remoto"
        coEvery { mockFirebaseApi.deleteTask(taskId) } just Runs

        // Act
        repository.deleteRemoteTask(taskId)

        // Assert
        coVerify(exactly = 1) { mockFirebaseApi.deleteTask(taskId) }
    }
}
