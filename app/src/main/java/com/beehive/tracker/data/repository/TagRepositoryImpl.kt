package com.beehive.tracker.data.repository

import com.beehive.tracker.data.local.dao.TagDao
import com.beehive.tracker.data.local.entity.toDomain
import com.beehive.tracker.data.local.entity.toEntity
import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val dao: TagDao,
) : TagRepository {

    override fun observeAll(): Flow<List<Tag>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(tag: Tag) = dao.insert(tag.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun getByIds(ids: List<String>): List<Tag> =
        dao.getByIds(ids).map { it.toDomain() }
}
