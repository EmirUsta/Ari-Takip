package com.beehive.tracker.domain.model

data class AudioRecord(
    val id: String,
    val noteId: String,
    val filePath: String,
    val durationSeconds: Int,
    val transcription: String?,
    val transcriptionStatus: TranscriptionStatus,
    val transcriptionVosk: String?,
    val transcriptionWhisper: String?,
    val createdAt: Long,
)
