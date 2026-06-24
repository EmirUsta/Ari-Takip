package com.beehive.tracker.domain.usecase.apiary

import com.beehive.tracker.domain.repository.ApiaryRepository
import javax.inject.Inject

// Arılığı ve tüm alt verilerini (kovan, not, ses kaydı) CASCADE ile siler.
// Ses dosyaları disk üzerinde bırakılır; Room sadece DB satırlarını kaldırır.
class DeleteApiaryUseCase @Inject constructor(
    private val repository: ApiaryRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
