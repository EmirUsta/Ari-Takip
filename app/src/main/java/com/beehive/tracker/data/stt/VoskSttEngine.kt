package com.beehive.tracker.data.stt

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.vosk.Model
import org.vosk.Recognizer
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoskSttEngine @Inject constructor(
    private val modelManager: ModelManager,
) {
    private var model: Model? = null

    suspend fun transcribe(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            if (modelManager.voskState.value != ModelState.READY) return@withContext null
            if (model == null) model = Model(modelManager.voskModelPath)

            val pcm = M4aToPcmDecoder.decode(filePath, 16000)
            val shorts = ShortArray(pcm.size) { i -> (pcm[i] * 32767).toInt().toShort() }

            val byteBuffer = ByteBuffer.allocate(shorts.size * 2).order(ByteOrder.LITTLE_ENDIAN)
            shorts.forEach { byteBuffer.putShort(it) }
            val bytes = byteBuffer.array()

            val recognizer = Recognizer(model, 16000.0f)
            val stream = ByteArrayInputStream(bytes)
            val buf = ByteArray(4096)
            var read: Int
            while (stream.read(buf).also { read = it } != -1) {
                recognizer.acceptWaveForm(buf, read)
            }
            val result = JSONObject(recognizer.finalResult).optString("text", "")
            recognizer.close()
            result.ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }
}
