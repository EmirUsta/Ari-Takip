package com.beehive.tracker.domain.usecase.apiary

import com.beehive.tracker.domain.model.Apiary
import com.beehive.tracker.domain.repository.ApiaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetApiarysUseCase @Inject constructor(
    private val repository: ApiaryRepository,
) {
    operator fun invoke(): Flow<List<Apiary>> = repository.observeAll()
}
