package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.model.LayoutMode
import com.beehive.tracker.domain.repository.HiveRepository
import java.util.UUID
import javax.inject.Inject

class CreateHiveUseCase @Inject constructor(
    private val repository: HiveRepository,
) {
    suspend operator fun invoke(
        apiaryId: String,
        name: String,
        posX: Float = 100f,
        posY: Float = 100f,
    ) {
        val now = System.currentTimeMillis()
        repository.insert(
            Hive(
                id = UUID.randomUUID().toString(),
                apiaryId = apiaryId,
                name = name,
                posX = posX,
                posY = posY,
                layoutMode = LayoutMode.FREE,
                currentTagId = null,
                currentTagColorHex = null,
                createdAt = now,
                updatedAt = now,
            )
        )
    }
}
