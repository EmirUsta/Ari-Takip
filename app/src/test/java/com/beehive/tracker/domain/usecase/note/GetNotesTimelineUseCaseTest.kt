package com.beehive.tracker.domain.usecase.note

import app.cash.turbine.test
import com.beehive.tracker.domain.model.Note
import com.beehive.tracker.domain.repository.NoteRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetNotesTimelineUseCaseTest {

    private val repository: NoteRepository = mockk()
    private val useCase = GetNotesTimelineUseCase(repository)

    private fun note(id: String, hiveId: String, createdAt: Long) = Note(
        id = id, hiveId = hiveId, tagId = null, tagColorHex = null,
        tagLabel = null, textContent = "İçerik $id", updateHiveColor = false,
        createdAt = createdAt,
    )

    @Test
    fun `doğru hiveId ile repository çağrılır ve notlar iletilir`() = runTest {
        every { repository.observeByHive("hive-A") } returns flowOf(
            listOf(note("n2", "hive-A", 2000L), note("n1", "hive-A", 1000L))
        )

        useCase("hive-A").test {
            val list = awaitItem()
            // DAO ORDER BY createdAt DESC yapar; en yeni önce gelmeli
            assertEquals(2, list.size)
            assertEquals("n2", list[0].id)
            assertEquals("n1", list[1].id)
            awaitComplete()
        }
    }

    @Test
    fun `hiç not yoksa boş liste gelir`() = runTest {
        every { repository.observeByHive("hive-B") } returns flowOf(emptyList())

        useCase("hive-B").test {
            assertEquals(emptyList<Note>(), awaitItem())
            awaitComplete()
        }
    }
}
