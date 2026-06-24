package com.beehive.tracker.domain.usecase.tag

import com.beehive.tracker.domain.repository.TagRepository
import javax.inject.Inject

class DeleteTagUseCase @Inject constructor(
    private val repository: TagRepository,
) {
    suspend operator fun invoke(id: String) = repository.delete(id)
}
