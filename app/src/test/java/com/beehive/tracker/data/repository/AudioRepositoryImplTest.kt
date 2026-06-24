package com.beehive.tracker.data.repository

import app.cash.turbine.test
import com.beehive.tracker.data.local.dao.AudioRecordDao
import com.beehive.tracker.data.local.entity.AudioRecordEntity
import com.beehive.tracker.domain.model.AudioRecord
import com.beehive.tracker.domain.model.TranscriptionStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AudioRepositoryImplTest {

    private val dao: AudioRecordDao = mockk()
    private val repository = AudioRepositoryImpl(dao)

    private fun entity(id: String, noteId: String) = AudioRecordEntity(
        id = id, noteId = noteId, filePath = "/audio/$id.m4a",
        durationSeconds = 15, transcription = null,
        transcriptionStatus = TranscriptionStatus.NONE.name,
        createdAt = 1000L,
    )

    @Test
    fun `observeByNote entity yoksa null doner`() = runTest {
        every { dao.observeByNote("note-1") } returns flowOf(null)

        repository.observeByNote("note-1").test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `observeByNote entity varsa domain modeline cevrilir`() = runTest {
        every { dao.observeByNote("note-1") } returns flowOf(entity("ar-1", "note-1"))

        repository.observeByNote("note-1").test {
            val result = awaitItem()
            assertNotNull(result)
            assertEquals("ar-1", result!!.id)
            assertEquals("note-1", result.noteId)
            assertEquals(TranscriptionStatus.NONE, result.transcriptionStatus)
            awaitComplete()
        }
    }

    @Test
    fun `insert domain modelini entity e cevirip DAO ya yollar`() = runTest {
        val slot = slot<AudioRecordEntity>()
        coEvery { dao.insert(capture(slot)) } returns Unit

        val domain = AudioRecord(
            id = "ar-99", noteId = "note-5", filePath = "/audio/ar-99.m4a",
            durationSeconds = 42, transcription = null,
            transcriptionStatus = TranscriptionStatus.NONE,
            createdAt = 9000L,
        )
        repository.insert(domain)

        assertEquals("ar-99", slot.captured.id)
        assertEquals("note-5", slot.captured.noteId)
        assertEquals(42, slot.captured.durationSeconds)
        assertEquals(TranscriptionStatus.NONE.name, slot.captured.transcriptionStatus)
    }

    @Test
    fun `updateStatus DAO ya dogru string deger yollar`() = runTest {
        coEvery { dao.updateStatus("ar-1", any()) } returns Unit

        repository.updateStatus("ar-1", TranscriptionStatus.PENDING)

        coVerify(exactly = 1) { dao.updateStatus("ar-1", TranscriptionStatus.PENDING.name) }
    }
}
