package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.domain.repository.HiveRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DeleteHiveUseCaseTest {

    private val repository: HiveRepository = mockk()
    private val useCase = DeleteHiveUseCase(repository)

    @Test
    fun `verilen ID ile repository delete cagrilir`() = runTest {
        coJustRun { repository.delete(any()) }

        useCase("hive-42")

        coVerify(exactly = 1) { repository.delete("hive-42") }
    }

    @Test
    fun `bos ID ile de repository delete cagrilir`() = runTest {
        coJustRun { repository.delete(any()) }

        useCase("")

        coVerify(exactly = 1) { repository.delete("") }
    }
}
