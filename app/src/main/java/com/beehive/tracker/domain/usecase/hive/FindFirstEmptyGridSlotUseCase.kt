package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.core.constants.GridConstants
import com.beehive.tracker.domain.model.Hive
import javax.inject.Inject
import kotlin.math.roundToInt

class FindFirstEmptyGridSlotUseCase @Inject constructor() {
    operator fun invoke(
        hives: List<Hive>,
        cols: Int,
        rows: Int,
        cellPx: Float,
        halfGap: Float,
    ): Pair<Float, Float> {
        val sx = GridConstants.START_X
        val sy = GridConstants.START_Y
        val occupied = hives.map { hive ->
            val col = ((hive.posX - sx - halfGap) / cellPx).roundToInt().coerceIn(0, cols - 1)
            val row = ((hive.posY - sy - halfGap) / cellPx).roundToInt().coerceIn(0, rows - 1)
            col to row
        }.toSet()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                if ((col to row) !in occupied) {
                    return (sx + col * cellPx + halfGap) to (sy + row * cellPx + halfGap)
                }
            }
        }
        return (sx + halfGap) to (sy + halfGap)
    }
}
