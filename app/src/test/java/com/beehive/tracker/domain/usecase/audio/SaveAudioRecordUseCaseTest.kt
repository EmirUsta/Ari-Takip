package com.beehive.tracker.domain.usecase.audio

import com.beehive.tracker.domain.model.AudioRecord
import com.beehive.tracker.domain.model.TranscriptionStatus
import com.beehive.tracker.domain.repository.AudioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class SaveAudioRecordUseCaseTest {

    private val repository: AudioRepository = mockk()
    private val useCase = SaveAudioRecordUseCase(repository)

    @Test
    fun `dogru noteId filePath ve durationSeconds ile AudioRecord kaydedilir`() = runTest {
        val slot = slot<AudioRecord>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase(noteId = "note-1", filePath = "/data/audio/abc.m4a", durationSeconds = 30)

        assertEquals("note-1", slot.captured.noteId)
        assertEquals("/data/audio/abc.m4a", slot.captured.filePath)
        assertEquals(30, slot.captured.durationSeconds)
    }

    @Test
    fun `transcriptionStatus baslangicta NONE olarak kaydedilir`() = runTest {
        val slot = slot<AudioRecord>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("note-1", "/data/audio/abc.m4a", 10)

        assertEquals(TranscriptionStatus.NONE, slot.captured.transcriptionStatus)
    }

    @Test
    fun `transcription alani baslangicta null olarak kaydedilir`() = runTest {
        val slot = slot<AudioRecord>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("note-1", "/data/audio/abc.m4a", 5)

        assertNull(slot.captured.transcription)
    }

    @Test
    fun `her cagri icin benzersiz UUID uretilir`() = runTest {
        val slots = mutableListOf<AudioRecord>()
        coEvery { repository.insert(capture(slots)) } returns Unit

        useCase("note-1", "/a.m4a", 1)
        useCase("note-2", "/b.m4a", 2)

        assertNotEquals(slots[0].id, slots[1].id)
    }

    @Test
    fun `createdAt sifirdan buyuk timestamp iceriyor`() = runTest {
        val slot = slot<AudioRecord>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("note-1", "/a.m4a", 10)

        assertTrue(slot.captured.createdAt > 0)
    }
}
