package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.domain.repository.HiveRepository
import javax.inject.Inject

class MoveHiveUseCase @Inject constructor(
    private val repository: HiveRepository,
) {
    suspend operator fun invoke(hiveId: String, posX: Float, posY: Float) {
        repository.updatePosition(hiveId, posX, posY)
    }
}
