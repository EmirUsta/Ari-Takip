package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.domain.repository.HiveRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MoveHiveUseCaseTest {

    private val repository: HiveRepository = mockk()
    private val useCase = MoveHiveUseCase(repository)

    @Test
    fun `doğru ID ve koordinatlarla repository çağrılır`() = runTest {
        coEvery { repository.updatePosition(any(), any(), any()) } returns Unit

        useCase("hive-42", 320f, 480f)

        coVerify(exactly = 1) { repository.updatePosition("hive-42", 320f, 480f) }
    }

    @Test
    fun `sıfır koordinat kabul edilir`() = runTest {
        coEvery { repository.updatePosition(any(), any(), any()) } returns Unit

        useCase("hive-1", 0f, 0f)

        coVerify { repository.updatePosition("hive-1", 0f, 0f) }
    }
}
