package com.beehive.tracker.domain.usecase.audio

import com.beehive.tracker.domain.model.AudioRecord
import com.beehive.tracker.domain.model.TranscriptionStatus
import com.beehive.tracker.domain.repository.AudioRepository
import java.util.UUID
import javax.inject.Inject

// Bir ses kaydını DB'ye yazar. STT başlangıçta NONE; ilerleyen fazda PENDING'e alınır.
class SaveAudioRecordUseCase @Inject constructor(
    private val repository: AudioRepository,
) {
    suspend operator fun invoke(noteId: String, filePath: String, durationSeconds: Int) {
        repository.insert(
            AudioRecord(
                id = UUID.randomUUID().toString(),
                noteId = noteId,
                filePath = filePath,
                durationSeconds = durationSeconds,
                transcription = null,
                transcriptionStatus = TranscriptionStatus.NONE,
                createdAt = System.currentTimeMillis(),
            )
        )
    }
}
