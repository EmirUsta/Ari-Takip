package com.beehive.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.beehive.tracker.data.local.entity.AudioRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioRecordDao {

    // 1-1 ilişki: bir notun en fazla bir ses kaydı olur
    @Query("SELECT * FROM audio_record WHERE noteId = :noteId LIMIT 1")
    fun observeByNote(noteId: String): Flow<AudioRecordEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AudioRecordEntity)

    // STT durumu güncellemesi; status TranscriptionStatus.name değeri
    @androidx.room.Query("UPDATE audio_record SET transcriptionStatus = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("SELECT * FROM audio_record WHERE noteId = :noteId LIMIT 1")
    suspend fun getByNoteId(noteId: String): AudioRecordEntity?

    @Query("SELECT * FROM audio_record WHERE createdAt < :cutoffMs")
    suspend fun getOlderThan(cutoffMs: Long): List<AudioRecordEntity>

    @Query("DELETE FROM audio_record WHERE id = :id")
    suspend fun deleteById(id: String)
}
