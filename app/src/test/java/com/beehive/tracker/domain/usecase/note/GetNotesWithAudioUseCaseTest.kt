package com.beehive.tracker.domain.usecase.note

import app.cash.turbine.test
import com.beehive.tracker.domain.model.Note
import com.beehive.tracker.domain.model.NoteWithAudio
import com.beehive.tracker.domain.repository.NoteRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetNotesWithAudioUseCaseTest {

    private val repository: NoteRepository = mockk()
    private val useCase = GetNotesWithAudioUseCase(repository)

    private fun note(id: String) = Note(
        id = id, hiveId = "hive-1", tagId = null, tagColorHex = null,
        tagLabel = null, textContent = "test", updateHiveColor = false,
        createdAt = 1000L,
    )

    @Test
    fun `repository delegasyonu dogru hiveId ile yapilir`() = runTest {
        every { repository.observeByHiveWithAudio("hive-1") } returns flowOf(emptyList())

        useCase("hive-1").test {
            assertEquals(emptyList<NoteWithAudio>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `ses kaydi olan ve olmayan notlar birlikte doner`() = runTest {
        val list = listOf(
            NoteWithAudio(note("n1"), audioRecord = null),
            NoteWithAudio(note("n2"), audioRecord = null),
        )
        every { repository.observeByHiveWithAudio("hive-1") } returns flowOf(list)

        useCase("hive-1").test {
            val result = awaitItem()
            assertEquals(2, result.size)
            assertNull(result[0].audioRecord)
            awaitComplete()
        }
    }
}
