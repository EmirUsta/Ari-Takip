package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.domain.repository.HiveRepository
import javax.inject.Inject

// Kovanı ve CASCADE kuralı gereği tüm notlarını siler.
// Silme sonrası Room Flow otomatik yeni liste yayar → MapScreen pin'i kaldırır.
class DeleteHiveUseCase @Inject constructor(
    private val repository: HiveRepository,
) {
    suspend operator fun invoke(hiveId: String) = repository.delete(hiveId)
}
