package com.beehive.tracker.domain.repository

import com.beehive.tracker.domain.model.Apiary
import kotlinx.coroutines.flow.Flow

interface ApiaryRepository {
    fun observeAll(): Flow<List<Apiary>>
    suspend fun insert(apiary: Apiary)
    suspend fun delete(id: String)
    suspend fun getById(id: String): Apiary?
}
