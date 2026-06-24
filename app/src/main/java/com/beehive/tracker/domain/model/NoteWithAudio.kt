package com.beehive.tracker.domain.model

// Zaman çizelgesi için not + ses kaydı birleşimi.
// audioRecord null → bu nota ses eklenmemiş.
data class NoteWithAudio(
    val note: Note,
    val audioRecord: AudioRecord?,
)
