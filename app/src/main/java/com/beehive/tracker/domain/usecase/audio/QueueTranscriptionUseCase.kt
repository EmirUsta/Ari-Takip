package com.beehive.tracker.domain.usecase.audio

import com.beehive.tracker.domain.model.TranscriptionStatus
import com.beehive.tracker.domain.repository.AudioRepository
import javax.inject.Inject

// Bir ses kaydını STT kuyruğuna alır: durumu PENDING yapar.
// Gerçek transkripsiyon arka plan WorkManager Worker'ında gerçekleşecek (Faz 6+ altyapısı).
// Şimdilik sadece durumu günceller; Worker bağlandığında PENDING kayıtları tarayacak.
class QueueTranscriptionUseCase @Inject constructor(
    private val audioRepository: AudioRepository,
) {
    suspend operator fun invoke(audioRecordId: String) {
        audioRepository.updateStatus(audioRecordId, TranscriptionStatus.PENDING)
    }
}
