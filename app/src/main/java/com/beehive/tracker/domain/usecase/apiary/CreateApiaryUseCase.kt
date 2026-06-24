package com.beehive.tracker.domain.usecase.apiary

import com.beehive.tracker.domain.model.Apiary
import com.beehive.tracker.domain.repository.ApiaryRepository
import java.util.UUID
import javax.inject.Inject

class CreateApiaryUseCase @Inject constructor(
    private val repository: ApiaryRepository,
) {
    suspend operator fun invoke(name: String, locationNote: String = "") {
        val now = System.currentTimeMillis()
        repository.insert(
            Apiary(
                id = UUID.randomUUID().toString(),
                name = name,
                locationNote = locationNote,
                createdAt = now,
                updatedAt = now,
            )
        )
    }
}
