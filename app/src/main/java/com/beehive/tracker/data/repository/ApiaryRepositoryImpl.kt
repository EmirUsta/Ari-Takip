package com.beehive.tracker.data.repository

import com.beehive.tracker.data.local.dao.ApiaryDao
import com.beehive.tracker.data.local.entity.toDomain
import com.beehive.tracker.data.local.entity.toEntity
import com.beehive.tracker.domain.model.Apiary
import com.beehive.tracker.domain.repository.ApiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ApiaryRepositoryImpl @Inject constructor(
    private val dao: ApiaryDao,
) : ApiaryRepository {

    override fun observeAll(): Flow<List<Apiary>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun insert(apiary: Apiary) = dao.insert(apiary.toEntity())

    override suspend fun delete(id: String) = dao.delete(id)

    override suspend fun getById(id: String): Apiary? = dao.getById(id)?.toDomain()
}
