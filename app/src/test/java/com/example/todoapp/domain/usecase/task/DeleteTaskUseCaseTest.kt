package com.example.todoapp.domain.usecase.task

import com.example.todoapp.domain.model.TaskWriteResult
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.domain.use_case.task.DeleteTaskUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import io.mockk.just
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class DeleteTaskUseCaseTest {

    @get:Rule
    val mockkRule = MockKRule(this)

    @RelaxedMockK
    private lateinit var mockRepository: TaskRepository

    private lateinit var deleteTaskUseCase: DeleteTaskUseCase

    private val testTaskId = "test-task-id-123"

    @Before
    fun setUp() {
        deleteTaskUseCase = DeleteTaskUseCase(mockRepository)
    }

    @Test
    fun `invoke con remoto exitoso devuelve Synced y borra local hard`() = runTest {
        coEvery { mockRepository.markPendingDelete(any(), any()) } just runs
        coEvery { mockRepository.deleteRemoteTask(any()) } just runs
        coEvery { mockRepository.deleteLocalTaskHard(any()) } just runs

        val result = deleteTaskUseCase(testTaskId)

        assertTrue(result is TaskWriteResult.Synced)
        coVerify(exactly = 1) { mockRepository.markPendingDelete(testTaskId, any()) }
        coVerify(exactly = 1) { mockRepository.deleteRemoteTask(testTaskId) }
        coVerify(exactly = 1) { mockRepository.deleteLocalTaskHard(testTaskId) }
    }

    @Test
    fun `invoke cuando falla remoto devuelve PendingSync y marca FAILED_DELETE`() = runTest {
        coEvery { mockRepository.markPendingDelete(any(), any()) } just runs
        coEvery { mockRepository.deleteRemoteTask(testTaskId) } throws RuntimeException("Error remoto")
        coEvery { mockRepository.markDeleteFailed(any(), any()) } just runs

        val result = deleteTaskUseCase(testTaskId)

        assertTrue(result is TaskWriteResult.PendingSync)
        coVerify(exactly = 1) { mockRepository.markPendingDelete(testTaskId, any()) }
        coVerify(exactly = 1) { mockRepository.deleteRemoteTask(testTaskId) }
        coVerify(exactly = 1) { mockRepository.markDeleteFailed(testTaskId, any()) }
    }
}
