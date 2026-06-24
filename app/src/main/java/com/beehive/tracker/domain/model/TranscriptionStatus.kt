package com.beehive.tracker.domain.model

// Ses kaydının konuşmadan metne (STT) dönüşüm durumu.
// STT servisi bağlanmadan önce NONE; bağlanınca PENDING → DONE/FAILED.
enum class TranscriptionStatus { NONE, PENDING, DONE, FAILED }
