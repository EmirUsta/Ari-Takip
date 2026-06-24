package com.beehive.tracker.data.local.dao

import androidx.room.Embedded
import androidx.room.Relation
import com.beehive.tracker.data.local.entity.AudioRecordEntity
import com.beehive.tracker.data.local.entity.NoteEntity
import com.beehive.tracker.data.local.entity.toDomain
import com.beehive.tracker.domain.model.NoteWithAudio

// Room @Relation ile note + audio_record LEFT JOIN sonucu.
// @Relation her zaman List döndürür; 1-1 ilişki için firstOrNull() kullanılır.
data class NoteWithAudioEntity(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "noteId",
        entity = AudioRecordEntity::class,
    )
    val audioRecords: List<AudioRecordEntity>,
) {
    fun toDomain() = NoteWithAudio(
        note = note.toDomain(),
        audioRecord = audioRecords.firstOrNull()?.toDomain(),
    )
}
