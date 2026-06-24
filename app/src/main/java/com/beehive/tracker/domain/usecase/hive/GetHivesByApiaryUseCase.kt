package com.beehive.tracker.domain.usecase.hive

import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.repository.HiveRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHivesByApiaryUseCase @Inject constructor(
    private val repository: HiveRepository,
) {
    operator fun invoke(apiaryId: String): Flow<List<Hive>> =
        repository.observeByApiary(apiaryId)
}
