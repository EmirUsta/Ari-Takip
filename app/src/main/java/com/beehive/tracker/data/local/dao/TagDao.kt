package com.beehive.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beehive.tracker.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {
    @Query("SELECT * FROM tag ORDER BY sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TagEntity)

    @Query("DELETE FROM tag WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM tag WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<TagEntity>
}
