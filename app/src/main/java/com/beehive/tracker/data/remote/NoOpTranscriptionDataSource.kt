package com.beehive.tracker.data.remote

import javax.inject.Inject

// STT servisi bağlanana kadar kullanılan boş implementasyon.
// Her zaman null döner → transkript üretilmez; uygulama kırılmaz.
class NoOpTranscriptionDataSource @Inject constructor() : TranscriptionRemoteDataSource {
    override suspend fun transcribe(filePath: String): String? = null
}
