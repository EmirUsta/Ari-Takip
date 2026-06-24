package com.beehive.tracker.domain.usecase.apiary

import com.beehive.tracker.domain.model.Apiary
import com.beehive.tracker.domain.repository.ApiaryRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CreateApiaryUseCaseTest {

    private val repository: ApiaryRepository = mockk()
    private val useCase = CreateApiaryUseCase(repository)

    @Test
    fun `arılık adı ve konum notu doğru kaydedilir`() = runTest {
        val slot = slot<Apiary>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("Kuzey Bahçesi", "Ormanın kenarı")

        val saved = slot.captured
        assertEquals("Kuzey Bahçesi", saved.name)
        assertEquals("Ormanın kenarı", saved.locationNote)
        assertNotNull(saved.id)
        assertTrue(saved.createdAt > 0)
        assertEquals(saved.createdAt, saved.updatedAt)
    }

    @Test
    fun `konum notu verilmezse boş string kaydedilir`() = runTest {
        val slot = slot<Apiary>()
        coEvery { repository.insert(capture(slot)) } returns Unit

        useCase("Güney Arılığı")

        assertEquals("", slot.captured.locationNote)
    }

    @Test
    fun `her çağrıda benzersiz UUID üretilir`() = runTest {
        val ids = mutableListOf<String>()
        coEvery { repository.insert(any()) } answers {
            ids.add(firstArg<Apiary>().id)
        }

        useCase("A")
        useCase("B")

        assertEquals(2, ids.distinct().size)
    }

    @Test
    fun `repository insert tam olarak bir kez çağrılır`() = runTest {
        coEvery { repository.insert(any()) } returns Unit

        useCase("Test Arılık")

        coVerify(exactly = 1) { repository.insert(any()) }
    }
}
