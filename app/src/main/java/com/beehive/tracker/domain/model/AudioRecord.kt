package com.beehive.tracker.domain.model

// Bir nota ait ses kaydı; noteId üzerinden Note ile 1-1 ilişki.
// filePath: cihaz yerel dosya sistemi yolu (filesDir/audio/*.m4a).
// transcription: STT tamamlandığında doldurulur; başlangıçta null.
data class AudioRecord(
    val id: String,
    val noteId: String,
    val filePath: String,
    val durationSeconds: Int,
    val transcription: String?,
    val transcriptionStatus: TranscriptionStatus,
    val createdAt: Long,
)
