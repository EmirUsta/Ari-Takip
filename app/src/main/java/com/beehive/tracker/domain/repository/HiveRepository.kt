package com.beehive.tracker.domain.repository

import com.beehive.tracker.domain.model.Hive
import kotlinx.coroutines.flow.Flow

interface HiveRepository {
    fun observeByApiary(apiaryId: String): Flow<List<Hive>>
    fun observeById(id: String): Flow<Hive?>   // HiveDetailScreen başlığı için
    suspend fun insert(hive: Hive)
    suspend fun updatePosition(id: String, posX: Float, posY: Float)
    suspend fun updateCurrentTag(id: String, tagId: String?)
    suspend fun delete(id: String)
    suspend fun getAllByApiary(apiaryId: String): List<Hive>
}
