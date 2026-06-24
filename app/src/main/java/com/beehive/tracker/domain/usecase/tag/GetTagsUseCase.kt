package com.beehive.tracker.domain.usecase.tag

import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTagsUseCase @Inject constructor(
    private val repository: TagRepository,
) {
    operator fun invoke(): Flow<List<Tag>> = repository.observeAll()
}
