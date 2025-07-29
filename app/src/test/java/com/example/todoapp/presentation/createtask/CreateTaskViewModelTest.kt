package com.example.todoapp.presentation.createtask

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import app.cash.turbine.test
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.use_case.task.AddTaskUseCase
import com.example.todoapp.presentation.features.task.create.viewmodel.CreateTaskUiState
import com.example.todoapp.presentation.features.task.create.viewmodel.CreateTaskViewModel
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@ExperimentalCoroutinesApi // Necesario para TestDispatcher y otras APIs de prueba de coroutines
class CreateTaskViewModelTest {

    // Regla para MockK (opcional si inicializas mocks manualmente, pero útil)
    @get:Rule
    val mockkRule = MockKRule(this)

    // Mock para el caso de uso
    @RelaxedMockK // RelaxedMockK devuelve valores por defecto para funciones no stubbeadas
    private lateinit var mockAddTaskUseCase: AddTaskUseCase

    // El ViewModel a probar
    private lateinit var viewModel: CreateTaskViewModel

    // TestDispatcher para controlar las coroutines en los tests
    private lateinit var testDispatcher: TestDispatcher

    private val testDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy",
        java.util.Locale("es", "MX")
    )
        .withZone(ZoneId.systemDefault())

    @Before
    fun setUp() {
        // 1. Configurar el TestDispatcher como el Dispatcher principal
        testDispatcher = StandardTestDispatcher() // CAMBIO AQUÍ
        Dispatchers.setMain(testDispatcher)

        // 2. Inicializar el ViewModel con el mock
        viewModel = CreateTaskViewModel(mockAddTaskUseCase)
    }

    @After
    fun tearDown() {
        // 3. Limpiar el Dispatcher principal después de cada test
        Dispatchers.resetMain()
    }

    @Test
    fun `init formatea correctamente endRepeatDateText`() = runTest {
        // Arrange: El estado inicial ya tiene una endRepeatDate por defecto
        val initialEndRepeatDate = CreateTaskUiState().endRepeatDate // Obtiene la fecha por defecto del UiState
        val expectedFormattedDate = testDateFormatter.format(initialEndRepeatDate)

        // Act: La inicialización ya ocurrió en setUp()

        // Assert
        assertEquals(expectedFormattedDate, viewModel.uiState.value.endRepeatDateText)
    }

    @Test
    fun `onTitleChange actualiza el titulo en uiState`() = runTest {
        val newTitle = "Nuevo Título de Prueba"
        viewModel.onTitleChange(newTitle)
        assertEquals(newTitle, viewModel.uiState.value.title)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `onDescriptionChange actualiza la descripcion en uiState`() = runTest {
        val newDescription = "Descripción de prueba"
        viewModel.onDescriptionChange(newDescription)
        assertEquals(newDescription, viewModel.uiState.value.description)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `onCategorySelected actualiza la categoria y cierra dropdown en uiState`() = runTest {
        val category = "Trabajo"
        viewModel.onCategoryDropdownExpandedChange(true) // Primero abre el dropdown
        viewModel.onCategorySelected(category)
        assertEquals(category, viewModel.uiState.value.selectedCategory)
        assertFalse(viewModel.uiState.value.isCategoryDropdownExpanded)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }

    // ... (Tests similares para onColorSelected, onRepeatDailyChange, etc.)

    @Test
    fun `onEndRepeatDateChange actualiza endRepeatDate y endRepeatDateText en uiState`() = runTest {
        val newDate = Instant.now().plusSeconds(86400) // Mañana
        val expectedFormattedDate = testDateFormatter.format(newDate)

        viewModel.onEndRepeatDateChange(newDate)

        assertEquals(newDate, viewModel.uiState.value.endRepeatDate)
        assertEquals(expectedFormattedDate, viewModel.uiState.value.endRepeatDateText)
        assertFalse(viewModel.uiState.value.saveSuccess)
    }


    @Test
    fun `saveTask con titulo vacio actualiza errorMessage y no llama al use case`() = runTest {
        // Arrange
        viewModel.onTitleChange("") // Título vacío

        // Act
        viewModel.saveTask()

        // Assert
        viewModel.uiState.test {
            val emission = awaitItem() // Estado después del intento de guardado
            assertEquals("El título no puede estar vacío", emission.errorMessage)
            assertFalse(emission.isSaving)
            assertFalse(emission.saveSuccess)
            // Verificar que el caso de uso NO fue llamado
            coVerify(exactly = 0) { mockAddTaskUseCase(any()) }
        }
    }

    @Test
    fun `saveTask cuando ya esta guardando no hace nada`() = runTest { // testScheduler estará disponible
        // Arrange
        viewModel.onTitleChange("Título válido")

        val job = Job()
        coEvery { mockAddTaskUseCase(any()) } coAnswers {
            job.join()
        }

        // Act
        viewModel.saveTask() // Primera llamada

        // Avanzar el scheduler para permitir que la actualización de _uiState.update { it.copy(isSaving = true) } ocurra
        // y la corrutina de viewModelScope.launch comience
        testScheduler.advanceUntilIdle() // Ejecuta todas las tareas pendientes

        // Assert para la primera llamada
        assertTrue("isSaving debería ser true después de la primera llamada y avanzar el scheduler", viewModel.uiState.value.isSaving)

        // Segunda llamada
        viewModel.saveTask()
        testScheduler.advanceUntilIdle() // Avanzar de nuevo por si acaso

        // Assert
        coVerify(exactly = 1) { mockAddTaskUseCase(any()) }
        assertTrue("isSaving aún debería ser true", viewModel.uiState.value.isSaving) // El estado no debería haber cambiado isSaving a false

        job.cancel()
    }



    @Test
    fun `saveTask con datos validos llama a AddTaskUseCase y actualiza uiState a exito`() = runTest {
        // Arrange
        val title = "Tarea Válida"
        val description = "Descripción"
        val selectedColor = Color.Blue
        val repeatTime = LocalTime.NOON
        val endRepeatDate = Instant.now().plusSeconds(3600) // En una hora
        val repeatDaily = true
        val endRepeatOption = "Until" // Asumiendo que esta opción usa endRepeatDate

        viewModel.onTitleChange(title)
        viewModel.onDescriptionChange(description)
        viewModel.onColorSelected(selectedColor)
        viewModel.onRepeatTimeChange(repeatTime)
        viewModel.onRepeatDailyChange(repeatDaily)
        viewModel.onEndRepeatOptionChange(endRepeatOption) // Para que se use endRepeatDate
        viewModel.onEndRepeatDateChange(endRepeatDate)

        // Capturar la tarea que se pasa al caso de uso
        val taskSlot = slot<Task>()
        coEvery { mockAddTaskUseCase(capture(taskSlot)) } just Runs // Mockea el caso de uso para que tenga éxito

        // Act
        viewModel.saveTask()

        // Assert
        viewModel.uiState.test {
            // El estado puede pasar por:
            // 1. Inicial (antes de saveTask)
            // 2. isSaving = true (dentro de saveTask, antes del launch)
            // 3. isSaving = true (dentro del launch, antes del use case)
            // 4. isSaving = false, saveSuccess = true (después del use case exitoso)

            // Esperamos la emisión donde isSaving es true (comienzo del guardado)
            var currentItem = awaitItem() // Estado inicial o después de cambios
            if (!currentItem.isSaving) currentItem = awaitItem() // Si el primer await no fue el de isSaving = true

            assertTrue("Debería estar guardando", currentItem.isSaving)
            assertNull("No debería haber mensaje de error", currentItem.errorMessage)

            // Esperamos la emisión final después del éxito
            currentItem = awaitItem()
            assertFalse("No debería estar guardando", currentItem.isSaving)
            assertTrue("Debería indicar éxito al guardar", currentItem.saveSuccess)
            assertNull("No debería haber mensaje de error", currentItem.errorMessage)

            // No más emisiones inesperadas
            // cancelAndIgnoreRemainingEvents() // Opcional si solo te importan las anteriores
        }

        // Verificar que el caso de uso fue llamado una vez
        coVerify(exactly = 1) { mockAddTaskUseCase(any()) }

        // Verificar los datos de la tarea capturada
        val capturedTask = taskSlot.captured
        assertEquals(title, capturedTask.title)
        assertEquals(description, capturedTask.description)
        assertEquals(selectedColor.toArgb(), capturedTask.color)
        assertEquals(repeatTime, capturedTask.repeatAt)
        assertEquals(repeatDaily, capturedTask.repeatDaily)
        // Para limitDate, la lógica es:
        // if (currentState.endRepeatOption != "Never" && currentState.repeatDaily) { currentState.endRepeatDate } else { Instant.now() }
        // En nuestro arrange, endRepeatOption = "Until" y repeatDaily = true, entonces debería ser endRepeatDate
        assertEquals(endRepeatDate, capturedTask.limitDate)

        assertNotNull(capturedTask.id) // Se debe generar un ID
        assertNotNull(capturedTask.createdAt) // Se debe generar una fecha de creación
    }

    @Test
    fun `saveTask cuando AddTaskUseCase falla actualiza uiState con mensaje de error`() = runTest {
        // Arrange
        viewModel.onTitleChange("Tarea que Fallará")
        val errorMessage = "Error simulado desde el repositorio"
        coEvery { mockAddTaskUseCase(any()) } throws Exception(errorMessage)

        // Act
        viewModel.saveTask()

        // Assert
        viewModel.uiState.test {
            var currentItem = awaitItem()
            if(!currentItem.isSaving) currentItem = awaitItem() // Espera a que isSaving sea true

            assertTrue(currentItem.isSaving)

            currentItem = awaitItem() // Espera el estado después del error
            assertFalse(currentItem.isSaving)
            assertFalse(currentItem.saveSuccess)
            assertTrue(currentItem.errorMessage?.contains(errorMessage) ?: false)
        }

        coVerify(exactly = 1) { mockAddTaskUseCase(any()) }
    }

    @Test
    fun `onSaveSuccessConsumed resetea saveSuccess`() = runTest {
        // Simular un guardado exitoso primero
        viewModel.onTitleChange("Test")
        coEvery { mockAddTaskUseCase(any()) } just Runs
        viewModel.saveTask()
        // Esperar a que saveSuccess sea true (usando Turbine o simplemente confiando en el dispatcher)
        // Para ser más robusto, se podría usar viewModel.uiState.first { it.saveSuccess }
        // o avanzar el dispatcher si se usa StandardTestDispatcher.
        // Con UnconfinedTestDispatcher, debería ser inmediato.
        // Forzamos el avance de la corrutina:
        testScheduler.advanceUntilIdle() // Si usas StandardTestDispatcher
        // Si usas UnconfinedTestDispatcher, la corrutina ya debería haber completado.

        assertTrue("saveSuccess debería ser true después de guardar", viewModel.uiState.value.saveSuccess)

        // Act
        viewModel.onSaveSuccessConsumed()

        // Assert
        assertFalse("saveSuccess debería ser false después de ser consumido", viewModel.uiState.value.saveSuccess)
    }

    @Test
    fun `onErrorMessageConsumed resetea errorMessage`() = runTest {
        // Simular un error
        viewModel.onTitleChange("Test")
        coEvery { mockAddTaskUseCase(any()) } throws Exception("error")
        viewModel.saveTask()
        testScheduler.advanceUntilIdle() // Si usas StandardTestDispatcher

        assertNotNull("errorMessage no debería ser null después de un error", viewModel.uiState.value.errorMessage)

        // Act
        viewModel.onErrorMessageConsumed()

        // Assert
        assertNull("errorMessage debería ser null después de ser consumido", viewModel.uiState.value.errorMessage)
    }
}