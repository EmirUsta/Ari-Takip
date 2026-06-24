package com.beehive.tracker.data.repository

import com.beehive.tracker.data.local.dao.NoteDao
import com.beehive.tracker.data.local.entity.toDomain
import com.beehive.tracker.data.local.entity.toEntity
import com.beehive.tracker.domain.model.Note
import com.beehive.tracker.domain.model.NoteWithAudio
import com.beehive.tracker.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
) : NoteRepository {

    override fun observeByHive(hiveId: String): Flow<List<Note>> =
        dao.observeByHive(hiveId).map { list -> list.map { it.toDomain() } }

    override fun observeByHiveWithAudio(hiveId: String): Flow<List<NoteWithAudio>> =
        dao.observeByHiveWithAudio(hiveId).map { list -> list.map { it.toDomain() } }

    override suspend fun insert(note: Note) = dao.insert(note.toEntity())

    override suspend fun getAllByHive(hiveId: String): List<Note> =
        dao.getAllByHive(hiveId).map { it.toDomain() }
}
