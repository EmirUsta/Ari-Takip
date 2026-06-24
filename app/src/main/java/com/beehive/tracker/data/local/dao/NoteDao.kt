package com.beehive.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.beehive.tracker.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // Zaman çizelgesi: en yeni not en üstte
    @Query("SELECT * FROM note WHERE hiveId = :hiveId ORDER BY createdAt DESC")
    fun observeByHive(hiveId: String): Flow<List<NoteEntity>>

    // @Transaction: Room birden fazla tablo okuyacağı için atomik garantisi gerekir
    @Transaction
    @Query("SELECT * FROM note WHERE hiveId = :hiveId ORDER BY createdAt DESC")
    fun observeByHiveWithAudio(hiveId: String): Flow<List<NoteWithAudioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: NoteEntity)

    @Query("SELECT * FROM note WHERE hiveId = :hiveId ORDER BY createdAt DESC")
    suspend fun getAllByHive(hiveId: String): List<NoteEntity>
}
