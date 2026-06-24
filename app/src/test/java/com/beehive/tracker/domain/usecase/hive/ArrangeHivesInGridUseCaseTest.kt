package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.domain.repository.HiveRepository
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ArrangeHivesInGridUseCaseTest {

    private val repository: HiveRepository = mockk()
    private val useCase = ArrangeHivesInGridUseCase(repository)

    @Test
    fun `bos liste verilince hicbir updatePosition cagrisi olmaz`() = runTest {
        coJustRun { repository.updatePosition(any(), any(), any()) }

        useCase(emptyList())

        coVerify(exactly = 0) { repository.updatePosition(any(), any(), any()) }
    }

    @Test
    fun `tek kovan START_X START_Y konumuna yerlestirilir`() = runTest {
        coJustRun { repository.updatePosition(any(), any(), any()) }

        useCase(listOf("h-1"))

        coVerify(exactly = 1) {
            repository.updatePosition(
                "h-1",
                ArrangeHivesInGridUseCase.START_X,
                ArrangeHivesInGridUseCase.START_Y,
            )
        }
    }

    @Test
    fun `4 kovan 4 farkli sutuna yerlestirilir - satir 0`() = runTest {
        coJustRun { repository.updatePosition(any(), any(), any()) }
        val ids = listOf("h-0", "h-1", "h-2", "h-3")

        useCase(ids)

        val sx = ArrangeHivesInGridUseCase.START_X
        val sy = ArrangeHivesInGridUseCase.START_Y
        val c = ArrangeHivesInGridUseCase.CELL

        coVerify { repository.updatePosition("h-0", sx + 0 * c, sy + 0 * c) }
        coVerify { repository.updatePosition("h-1", sx + 1 * c, sy + 0 * c) }
        coVerify { repository.updatePosition("h-2", sx + 2 * c, sy + 0 * c) }
        coVerify { repository.updatePosition("h-3", sx + 3 * c, sy + 0 * c) }
    }

    @Test
    fun `besinci kovan ikinci satirin ilk sutununa (0-1) yerlestirilir`() = runTest {
        coJustRun { repository.updatePosition(any(), any(), any()) }
        val ids = listOf("h-0", "h-1", "h-2", "h-3", "h-4")

        useCase(ids)

        coVerify {
            repository.updatePosition(
                "h-4",
                ArrangeHivesInGridUseCase.START_X + 0 * ArrangeHivesInGridUseCase.CELL,
                ArrangeHivesInGridUseCase.START_Y + 1 * ArrangeHivesInGridUseCase.CELL,
            )
        }
    }

    @Test
    fun `9 kovan 3 satira dogru dagilir - toplam 9 cagri`() = runTest {
        coJustRun { repository.updatePosition(any(), any(), any()) }
        val ids = (0 until 9).map { "h-$it" }

        useCase(ids)

        coVerify(exactly = 9) { repository.updatePosition(any(), any(), any()) }
    }
}
