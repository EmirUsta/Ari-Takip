package com.beehive.tracker.domain.usecase.note

import com.beehive.tracker.domain.model.Note
import com.beehive.tracker.domain.repository.HiveRepository
import com.beehive.tracker.domain.repository.NoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class AddNoteUseCaseTest {

    private val noteRepository: NoteRepository = mockk()
    private val hiveRepository: HiveRepository = mockk()
    private val useCase = AddNoteUseCase(noteRepository, hiveRepository)

    @Test
    fun `not doğru hiveId ve metin içeriğiyle kaydedilir`() = runTest {
        val slot = slot<Note>()
        coEvery { noteRepository.insert(capture(slot)) } returns Unit

        useCase(
            hiveId = "hive-1",
            tagId = null,
            tagColorHex = null,
            tagLabel = null,
            textContent = "Arılar sakin, bal çerçeveleri dolu.",
            updateHiveColor = false,
        )

        assertEquals("hive-1", slot.captured.hiveId)
        assertEquals("Arılar sakin, bal çerçeveleri dolu.", slot.captured.textContent)
        assertTrue(slot.captured.createdAt > 0)
        assertNotNull(slot.captured.id)
    }

    @Test
    fun `updateHiveColor true ve tagId varsa hiveRepository güncellenir`() = runTest {
        coEvery { noteRepository.insert(any()) } returns Unit
        coEvery { hiveRepository.updateCurrentTag(any(), any()) } returns Unit

        useCase(
            hiveId = "hive-1",
            tagId = "tag-red",
            tagColorHex = "#E53935",
            tagLabel = "Ana Arı Yok",
            textContent = "Kontrol yapıldı.",
            updateHiveColor = true,
        )

        // Hem not kaydedilmeli hem kovan rengi güncellenmeli
        coVerify(exactly = 1) { noteRepository.insert(any()) }
        coVerify(exactly = 1) { hiveRepository.updateCurrentTag("hive-1", "tag-red") }
    }

    @Test
    fun `updateHiveColor false ise hiveRepository hiç çağrılmaz`() = runTest {
        coEvery { noteRepository.insert(any()) } returns Unit

        useCase(
            hiveId = "hive-1",
            tagId = "tag-red",
            tagColorHex = "#E53935",
            tagLabel = "Ana Arı Yok",
            textContent = "Gözlem notu.",
            updateHiveColor = false,
        )

        coVerify(exactly = 0) { hiveRepository.updateCurrentTag(any(), any()) }
    }

    @Test
    fun `tagId null iken updateHiveColor true olsa bile hiveRepository çağrılmaz`() = runTest {
        coEvery { noteRepository.insert(any()) } returns Unit

        useCase(
            hiveId = "hive-1",
            tagId = null,       // etiket seçilmemiş
            tagColorHex = null,
            tagLabel = null,
            textContent = "Metin var ama etiket yok.",
            updateHiveColor = true,
        )

        // Etiket yoksa renk güncellemesinin anlamı olmadığından çağrılmamalı
        coVerify(exactly = 0) { hiveRepository.updateCurrentTag(any(), any()) }
    }

    @Test
    fun `etiket bilgileri (colorHex ve label) nota denormalize olarak kaydedilir`() = runTest {
        val slot = slot<Note>()
        coEvery { noteRepository.insert(capture(slot)) } returns Unit
        coEvery { hiveRepository.updateCurrentTag(any(), any()) } returns Unit

        useCase(
            hiveId = "hive-1",
            tagId = "tag-green",
            tagColorHex = "#43A047",
            tagLabel = "Bal Durumu İyi",
            textContent = null,
            updateHiveColor = true,
        )

        assertEquals("#43A047", slot.captured.tagColorHex)
        assertEquals("Bal Durumu İyi", slot.captured.tagLabel)
    }

    @Test
    fun `metin ve etiket ikisi de null olan not kaydedilebilir`() = runTest {
        val slot = slot<Note>()
        coEvery { noteRepository.insert(capture(slot)) } returns Unit

        useCase("hive-1", null, null, null, null, false)

        assertNull(slot.captured.textContent)
        assertNull(slot.captured.tagId)
        assertNotNull(slot.captured.id)   // UUID yine de üretilmeli
    }
}
