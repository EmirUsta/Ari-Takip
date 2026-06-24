package com.beehive.tracker.data.repository

import app.cash.turbine.test
import com.beehive.tracker.data.local.dao.NoteDao
import com.beehive.tracker.data.local.entity.NoteEntity
import com.beehive.tracker.domain.model.Note
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

class NoteRepositoryImplTest {

    private val dao: NoteDao = mockk()
    private val repository = NoteRepositoryImpl(dao)

    private fun entity(id: String, hiveId: String) = NoteEntity(
        id = id, hiveId = hiveId, tagId = null, tagColorHex = null,
        tagLabel = null, textContent = "Test notu", updateHiveColor = false,
        createdAt = 1000L,
    )

    @Test
    fun `observeByHive entity listesini domain modeline çevirir`() = runTest {
        every { dao.observeByHive("hive-1") } returns flowOf(
            listOf(entity("n1", "hive-1"), entity("n2", "hive-1"))
        )

        repository.observeByHive("hive-1").test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertEquals("n1", list[0].id)
            assertEquals("Test notu", list[0].textContent)
            awaitComplete()
        }
    }

    @Test
    fun `insert domain modelini entity'e çevirip DAO'ya yollar`() = runTest {
        val slot = slot<NoteEntity>()
        coEvery { dao.insert(capture(slot)) } returns Unit

        val note = Note(
            id = "note-99",
            hiveId = "hive-1",
            tagId = "tag-1",
            tagColorHex = "#E53935",
            tagLabel = "Ana Arı Yok",
            textContent = "Kontrol yapıldı.",
            updateHiveColor = true,
            createdAt = 5000L,
        )
        repository.insert(note)

        assertEquals("note-99", slot.captured.id)
        assertEquals("hive-1", slot.captured.hiveId)
        assertEquals("tag-1", slot.captured.tagId)
        assertEquals("#E53935", slot.captured.tagColorHex)
        assertEquals("Ana Arı Yok", slot.captured.tagLabel)
        assertEquals(true, slot.captured.updateHiveColor)
    }

    @Test
    fun `null alanlar entity'e doğru aktarılır`() = runTest {
        val slot = slot<NoteEntity>()
        coEvery { dao.insert(capture(slot)) } returns Unit

        val note = Note("id", "hive-1", null, null, null, null, false, 1L)
        repository.insert(note)

        assertNull(slot.captured.tagId)
        assertNull(slot.captured.textContent)
    }
}
