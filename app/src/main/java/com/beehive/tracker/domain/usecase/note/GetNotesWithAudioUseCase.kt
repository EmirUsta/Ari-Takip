package com.beehive.tracker.domain.usecase.note

import com.beehive.tracker.domain.model.NoteWithAudio
import com.beehive.tracker.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Kovana ait notları ses kayıtlarıyla birlikte getirir.
// HiveDetailViewModel bu use case'i kullanır; eski GetNotesTimelineUseCase değiştirilmez.
class GetNotesWithAudioUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
) {
    operator fun invoke(hiveId: String): Flow<List<NoteWithAudio>> =
        noteRepository.observeByHiveWithAudio(hiveId)
}
