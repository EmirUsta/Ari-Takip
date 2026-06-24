package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.core.constants.GridConstants
import com.beehive.tracker.domain.repository.HiveRepository
import javax.inject.Inject

class ArrangeHivesInGridUseCase @Inject constructor(
    private val repository: HiveRepository,
) {
    suspend operator fun invoke(hiveIds: List<String>, cols: Int, cellPx: Float, halfGap: Float) {
        hiveIds.forEachIndexed { index, id ->
            val col = index % cols
            val row = index / cols
            repository.updatePosition(
                id = id,
                posX = GridConstants.START_X + col * cellPx + halfGap,
                posY = GridConstants.START_Y + row * cellPx + halfGap,
            )
        }
    }
}
