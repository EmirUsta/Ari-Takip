package com.beehive.tracker.domain.usecase.tag

import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.repository.TagRepository
import java.util.UUID
import javax.inject.Inject

class CreateTagUseCase @Inject constructor(
    private val repository: TagRepository,
) {
    suspend operator fun invoke(label: String, colorHex: String, sortOrder: Int = 0) {
        repository.insert(
            Tag(
                id = UUID.randomUUID().toString(),
                label = label,
                colorHex = colorHex,
                sortOrder = sortOrder,
                createdAt = System.currentTimeMillis(),
            )
        )
    }
}
