package com.beehive.tracker.domain.usecase.tag

import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.repository.TagRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SeedDefaultTagsUseCaseTest {

    private val repository: TagRepository = mockk()
    private val useCase = SeedDefaultTagsUseCase(repository)

    @Test
    fun `etiket tablosu boşsa 5 varsayılan etiket eklenir`() = runTest {
        coEvery { repository.observeAll() } returns flowOf(emptyList())
        coEvery { repository.insert(any()) } returns Unit

        useCase()

        coVerify(exactly = 5) { repository.insert(any()) }
    }

    @Test
    fun `etiketler zaten varsa insert çağrılmaz`() = runTest {
        val existing = listOf(
            Tag("1", "Mevcut", "#FF0000", 0, System.currentTimeMillis())
        )
        coEvery { repository.observeAll() } returns flowOf(existing)

        useCase()

        coVerify(exactly = 0) { repository.insert(any()) }
    }

    @Test
    fun `seed edilen her etiketin rengi geçerli hex formatındadır`() = runTest {
        val inserted = mutableListOf<Tag>()
        coEvery { repository.observeAll() } returns flowOf(emptyList())
        coEvery { repository.insert(any()) } answers { inserted.add(firstArg()) }

        useCase()

        inserted.forEach { tag ->
            val isValidHex = tag.colorHex.matches(Regex("^#[0-9A-Fa-f]{6}$"))
            assertEquals("${tag.label} rengi geçersiz: ${tag.colorHex}", true, isValidHex)
        }
    }

    @Test
    fun `seed edilen etiketlerin sortOrder değerleri sıralıdır`() = runTest {
        val inserted = mutableListOf<Tag>()
        coEvery { repository.observeAll() } returns flowOf(emptyList())
        coEvery { repository.insert(any()) } answers { inserted.add(firstArg()) }

        useCase()

        val orders = inserted.map { it.sortOrder }
        assertEquals(orders.sorted(), orders)
    }

    @Test
    fun `seed edilen etiketlerin isimleri boş değildir`() = runTest {
        val inserted = mutableListOf<Tag>()
        coEvery { repository.observeAll() } returns flowOf(emptyList())
        coEvery { repository.insert(any()) } answers { inserted.add(firstArg()) }

        useCase()

        inserted.forEach { tag ->
            assertEquals("Boş label: $tag", true, tag.label.isNotBlank())
        }
    }
}
