package com.beehive.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beehive.tracker.data.local.entity.ApiaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiaryDao {
    @Query("SELECT * FROM apiary ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<ApiaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ApiaryEntity)

    @Query("DELETE FROM apiary WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM apiary WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ApiaryEntity?
}
