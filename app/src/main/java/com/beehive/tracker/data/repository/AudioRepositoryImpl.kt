package com.beehive.tracker.data.repository

import com.beehive.tracker.data.local.dao.AudioRecordDao
import com.beehive.tracker.data.local.entity.toDomain
import com.beehive.tracker.data.local.entity.toEntity
import com.beehive.tracker.domain.model.AudioRecord
import com.beehive.tracker.domain.model.TranscriptionStatus
import com.beehive.tracker.domain.repository.AudioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AudioRepositoryImpl @Inject constructor(
    private val dao: AudioRecordDao,
) : AudioRepository {

    override fun observeByNote(noteId: String): Flow<AudioRecord?> =
        dao.observeByNote(noteId).map { it?.toDomain() }

    override suspend fun insert(audioRecord: AudioRecord) = dao.insert(audioRecord.toEntity())

    override suspend fun updateStatus(id: String, status: TranscriptionStatus) =
        dao.updateStatus(id, status.name)

    override suspend fun getByNoteId(noteId: String): AudioRecord? =
        dao.getByNoteId(noteId)?.toDomain()

    override suspend fun getOlderThan(cutoffMs: Long): List<AudioRecord> =
        dao.getOlderThan(cutoffMs).map { it.toDomain() }

    override suspend fun deleteById(id: String) = dao.deleteById(id)
    override suspend fun updateVosk(id: String, text: String?) = dao.updateVosk(id, text)
    override suspend fun updateWhisper(id: String, text: String?) = dao.updateWhisper(id, text)
}
