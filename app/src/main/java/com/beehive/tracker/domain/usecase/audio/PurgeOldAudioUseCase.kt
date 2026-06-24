package com.beehive.tracker.domain.usecase.audio

import com.beehive.tracker.domain.repository.AudioRepository
import java.io.File
import javax.inject.Inject

class PurgeOldAudioUseCase @Inject constructor(
    private val repository: AudioRepository,
) {
    suspend operator fun invoke(ttlDays: Int) {
        if (ttlDays <= 0) return
        val cutoffMs = System.currentTimeMillis() - ttlDays * 24L * 60 * 60 * 1000
        val old = repository.getOlderThan(cutoffMs)
        old.forEach { record ->
            File(record.filePath).delete()
            repository.deleteById(record.id)
        }
    }
}
