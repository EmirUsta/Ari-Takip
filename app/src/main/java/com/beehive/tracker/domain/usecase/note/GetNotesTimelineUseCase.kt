package com.beehive.tracker.domain.usecase.note

import com.beehive.tracker.domain.model.Note
import com.beehive.tracker.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotesTimelineUseCase @Inject constructor(
    private val repository: NoteRepository,
) {
    // Zaman çizelgesi en yeni→en eski sırada gelir (DAO'da ORDER BY createdAt DESC)
    operator fun invoke(hiveId: String): Flow<List<Note>> =
        repository.observeByHive(hiveId)
}
