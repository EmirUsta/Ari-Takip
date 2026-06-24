package com.beehive.tracker.data.repository

import app.cash.turbine.test
import com.beehive.tracker.data.local.dao.ApiaryDao
import com.beehive.tracker.data.local.entity.ApiaryEntity
import com.beehive.tracker.domain.model.Apiary
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiaryRepositoryImplTest {

    private val dao: ApiaryDao = mockk()
    private val repository = ApiaryRepositoryImpl(dao)

    private fun entity(id: String, name: String) =
        ApiaryEntity(id, name, "", System.currentTimeMillis(), System.currentTimeMillis())

    @Test
    fun `observeAll entity listesini domain modeline çevirir`() = runTest {
        every { dao.observeAll() } returns flowOf(
            listOf(entity("1", "Kuzey"), entity("2", "Güney"))
        )

        repository.observeAll().test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals("Kuzey", list[0].name)
            assertEquals("Güney", list[1].name)
            awaitComplete()
        }
    }

    @Test
    fun `insert domain modelini entity'e çevirip DAO'ya yollar`() = runTest {
        val slot = slot<ApiaryEntity>()
        coEvery { dao.insert(capture(slot)) } returns Unit

        val apiary = Apiary("id-1", "Orman Arılığı", "Kuzey", 1000L, 1000L)
        repository.insert(apiary)

        assertEquals("id-1", slot.captured.id)
        assertEquals("Orman Arılığı", slot.captured.name)
        assertEquals("Kuzey", slot.captured.locationNote)
    }

    @Test
    fun `delete doğru ID ile DAO'yu çağırır`() = runTest {
        coEvery { dao.delete(any()) } returns Unit

        repository.delete("apiary-99")

        coVerify(exactly = 1) { dao.delete("apiary-99") }
    }
}
