package com.beehive.tracker.domain.repository

import com.beehive.tracker.domain.model.Note
import com.beehive.tracker.domain.model.NoteWithAudio
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    // Kovana ait notlar; en yeni en üstte (createdAt DESC)
    fun observeByHive(hiveId: String): Flow<List<Note>>
    // Ses kayıtlarıyla birlikte — HiveDetail zaman çizelgesi için
    fun observeByHiveWithAudio(hiveId: String): Flow<List<NoteWithAudio>>
    suspend fun insert(note: Note)
    suspend fun getAllByHive(hiveId: String): List<Note>
}
