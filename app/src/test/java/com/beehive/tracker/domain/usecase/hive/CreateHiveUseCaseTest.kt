package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.model.LayoutMode
import com.beehive.tracker.domain.repository.HiveRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CreateHiveUseCaseTest {

    private val repository: HiveRepository = mockk()
    private val useCase = CreateHiveUseCase(repository)

    @Test
    fun `kovan doğru arılık ID ile kaydedilir`() = runTest {
        val slot = slot<Hive>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase(apiaryId = "apiary-1", name = "Kovan A", posX = 150f, posY = 200f)

        assertEquals("apiary-1", slot.captured.apiaryId)
        assertEquals("Kovan A", slot.captured.name)
        assertEquals(150f, slot.captured.posX)
        assertEquals(200f, slot.captured.posY)
    }

    @Test
    fun `yeni kovanın varsayılan layout modu FREE olur`() = runTest {
        val slot = slot<Hive>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("apiary-1", "Kovan B")

        assertEquals(LayoutMode.FREE, slot.captured.layoutMode)
    }

    @Test
    fun `yeni kovanın etiketi başlangıçta null olur`() = runTest {
        val slot = slot<Hive>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("apiary-1", "Kovan C")

        assertNull(slot.captured.currentTagId)
        assertNull(slot.captured.currentTagColorHex)
    }

    @Test
    fun `pozisyon verilmezse varsayılan koordinatlar kullanılır`() = runTest {
        val slot = slot<Hive>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("apiary-1", "Kovan D")

        assertEquals(100f, slot.captured.posX)
        assertEquals(100f, slot.captured.posY)
    }

    @Test
    fun `repository insert bir kez çağrılır`() = runTest {
        coEvery { repository.insert(any()) } returns Unit

        useCase("apiary-1", "Kovan E")

        coVerify(exactly = 1) { repository.insert(any()) }
    }
}
