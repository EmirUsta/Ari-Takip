package com.beehive.tracker.domain.usecase.tag

import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.repository.TagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateTagUseCaseTest {

    private val repository: TagRepository = mockk()
    private val useCase = CreateTagUseCase(repository)

    @Test
    fun `etiket doğru label ve renk ile kaydedilir`() = runTest {
        val slot = slot<Tag>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("Şurup Verilecek", "#FDD835", sortOrder = 2)

        assertEquals("Şurup Verilecek", slot.captured.label)
        assertEquals("#FDD835", slot.captured.colorHex)
        assertEquals(2, slot.captured.sortOrder)
    }

    @Test
    fun `createdAt pozitif değer alır`() = runTest {
        val slot = slot<Tag>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("Test", "#000000")

        assertTrue(slot.captured.createdAt > 0)
    }

    @Test
    fun `repository insert bir kez çağrılır`() = runTest {
        coEvery { repository.insert(any()) } returns Unit

        useCase("Label", "#AABBCC")

        coVerify(exactly = 1) { repository.insert(any()) }
    }
}
