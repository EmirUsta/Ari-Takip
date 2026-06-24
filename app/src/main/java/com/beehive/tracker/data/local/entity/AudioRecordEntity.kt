package com.beehive.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.beehive.tracker.domain.model.AudioRecord
import com.beehive.tracker.domain.model.TranscriptionStatus

// Ses kaydı Room tablosu. Note silinince CASCADE ile birlikte silinir.
// transcriptionStatus String olarak saklanır; enum.name ↔ enum.valueOf dönüşümü toDomain/toEntity'de.
@Entity(
    tableName = "audio_record",
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("noteId")],
)
data class AudioRecordEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val filePath: String,
    val durationSeconds: Int,
    val transcription: String?,
    val transcriptionStatus: String,
    val createdAt: Long,
)

fun AudioRecordEntity.toDomain() = AudioRecord(
    id = id,
    noteId = noteId,
    filePath = filePath,
    durationSeconds = durationSeconds,
    transcription = transcription,
    transcriptionStatus = TranscriptionStatus.valueOf(transcriptionStatus),
    createdAt = createdAt,
)

fun AudioRecord.toEntity() = AudioRecordEntity(
    id = id,
    noteId = noteId,
    filePath = filePath,
    durationSeconds = durationSeconds,
    transcription = transcription,
    transcriptionStatus = transcriptionStatus.name,
    createdAt = createdAt,
)
