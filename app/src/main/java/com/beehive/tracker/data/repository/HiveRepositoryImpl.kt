package com.beehive.tracker.data.repository

import com.beehive.tracker.data.local.dao.HiveDao
import com.beehive.tracker.data.local.entity.toDomain
import com.beehive.tracker.data.local.entity.toEntity
import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.repository.HiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HiveRepositoryImpl @Inject constructor(
    private val dao: HiveDao,
) : HiveRepository {

    override fun observeByApiary(apiaryId: String): Flow<List<Hive>> =
        dao.observeByApiary(apiaryId).map { list -> list.map { it.toDomain() } }

    override fun observeById(id: String): Flow<Hive?> =
        dao.observeById(id).map { it?.toDomain() }

    override suspend fun insert(hive: Hive) = dao.insert(hive.toEntity())

    override suspend fun updatePosition(id: String, posX: Float, posY: Float) =
        dao.updatePosition(id, posX, posY)

    override suspend fun updateCurrentTag(id: String, tagId: String?) =
        dao.updateCurrentTag(id, tagId)

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun getAllByApiary(apiaryId: String): List<Hive> =
        dao.getAllByApiary(apiaryId).map { it.toDomain() }
}
