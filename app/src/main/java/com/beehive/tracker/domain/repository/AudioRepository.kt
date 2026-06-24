package com.beehive.tracker.domain.repository

import com.beehive.tracker.domain.model.AudioRecord
import com.beehive.tracker.domain.model.TranscriptionStatus
import kotlinx.coroutines.flow.Flow

interface AudioRepository {
    // Bir notun ses kaydını izler; kayıt yoksa null akışı
    fun observeByNote(noteId: String): Flow<AudioRecord?>
    suspend fun insert(audioRecord: AudioRecord)
    // STT kuyruğu için durum güncellemesi (NONE → PENDING → DONE/FAILED)
    suspend fun updateStatus(id: String, status: TranscriptionStatus)
    suspend fun getByNoteId(noteId: String): AudioRecord?
    suspend fun getOlderThan(cutoffMs: Long): List<AudioRecord>
    suspend fun deleteById(id: String)
    suspend fun updateVosk(id: String, text: String?)
    suspend fun updateWhisper(id: String, text: String?)
}
