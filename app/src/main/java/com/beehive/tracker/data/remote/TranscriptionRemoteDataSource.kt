package com.beehive.tracker.data.remote

// STT (konuşmadan metne) servis sözleşmesi.
// Faz 6 iskeleti: NoOp implementasyon şimdi bağlanır;
// gerçek STT servisi (Whisper, Google STT vb.) ileride bu interface'i implement eder.
interface TranscriptionRemoteDataSource {
    // filePath yerel ses dosyası; null döner → servis bağlı değil veya hata
    suspend fun transcribe(filePath: String): String?
}
