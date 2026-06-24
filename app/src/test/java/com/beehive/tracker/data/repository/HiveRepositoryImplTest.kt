package com.beehive.tracker.data.repository

import app.cash.turbine.test
import com.beehive.tracker.data.local.dao.HiveDao
import com.beehive.tracker.data.local.entity.HiveEntity
import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.model.LayoutMode
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HiveRepositoryImplTest {

    private val dao: HiveDao = mockk()
    private val repository = HiveRepositoryImpl(dao)

    private fun entity(id: String, name: String, apiaryId: String = "ap-1") = HiveEntity(
        id = id, apiaryId = apiaryId, name = name,
        posX = 0f, posY = 0f, layoutMode = "FREE",
        currentTagId = null, currentTagColorHex = null,
        createdAt = 1000L, updatedAt = 1000L,
    )

    @Test
    fun `observeByApiary entity listesini domain modeline çevirir`() = runTest {
        every { dao.observeByApiary("ap-1") } returns flowOf(
            listOf(entity("h1", "Kovan 1"), entity("h2", "Kovan 2"))
        )

        repository.observeByApiary("ap-1").test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals("Kovan 1", list[0].name)
            assertEquals(LayoutMode.FREE, list[0].layoutMode)
            awaitComplete()
        }
    }

    @Test
    fun `insert domain modelini entity'e çevirip DAO'ya yollar`() = runTest {
        val slot = slot<HiveEntity>()
        coEvery { dao.insert(capture(slot)) } returns Unit

        val hive = Hive("h1", "ap-1", "Kovan A", 100f, 200f, LayoutMode.FREE, null, null, 1L, 1L)
        repository.insert(hive)

        assertEquals("h1", slot.captured.id)
        assertEquals("ap-1", slot.captured.apiaryId)
        assertEquals(100f, slot.captured.posX)
        assertEquals(200f, slot.captured.posY)
        assertEquals("FREE", slot.captured.layoutMode)
        assertNull(slot.captured.currentTagId)
    }

    @Test
    fun `updatePosition doğru parametrelerle DAO'yu çağırır`() = runTest {
        // DAO imzası: updatePosition(id, posX, posY, now=currentTimeMillis())
        // 'now' default parametresi mock kurulumu ile gerçek çağrı arasında değişeceğinden any() kullanılır.
        coEvery { dao.updatePosition(any(), any(), any(), any()) } returns Unit

        repository.updatePosition("h-1", 350f, 420f)

        coVerify(exactly = 1) { dao.updatePosition("h-1", 350f, 420f, any()) }
    }

    @Test
    fun `updateCurrentTag doğru hive ID ve tag ID ile çağrılır`() = runTest {
        coEvery { dao.updateCurrentTag(any(), any(), any()) } returns Unit

        repository.updateCurrentTag("h-1", "tag-abc")

        coVerify(exactly = 1) { dao.updateCurrentTag("h-1", "tag-abc", any()) }
    }

    @Test
    fun `updateCurrentTag null tag ID kabul eder`() = runTest {
        coEvery { dao.updateCurrentTag(any(), any(), any()) } returns Unit

        repository.updateCurrentTag("h-1", null)

        coVerify { dao.updateCurrentTag("h-1", null, any()) }
    }
}
