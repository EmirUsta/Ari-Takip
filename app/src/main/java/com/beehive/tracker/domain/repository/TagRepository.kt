package com.beehive.tracker.domain.repository

import com.beehive.tracker.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun observeAll(): Flow<List<Tag>>
    suspend fun insert(tag: Tag)
    suspend fun delete(id: String)
    suspend fun getByIds(ids: List<String>): List<Tag>
}
