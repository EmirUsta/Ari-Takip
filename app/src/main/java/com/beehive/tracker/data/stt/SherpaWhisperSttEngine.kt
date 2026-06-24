package com.beehive.tracker.data.stt

import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SherpaWhisperSttEngine @Inject constructor(
    private val modelManager: ModelManager,
) {
    private var recognizer: OfflineRecognizer? = null

    private fun buildRecognizer(): OfflineRecognizer {
        val dir = modelManager.whisperModelDir
        val whisperConfig = OfflineWhisperModelConfig(
            encoder = "$dir/tiny-encoder.int8.onnx",
            decoder = "$dir/tiny-decoder.int8.onnx",
            language = "tr",
            task = "transcribe",
            tailPaddings = 1000,
        )
        val modelConfig = OfflineModelConfig(
            whisper = whisperConfig,
            tokens = "$dir/tiny-tokens.txt",
            numThreads = 2,
            debug = false,
        )
        val config = OfflineRecognizerConfig(
            featConfig = FeatureConfig(sampleRate = 16000, featureDim = 80),
            modelConfig = modelConfig,
        )
        return OfflineRecognizer(config = config)
    }

    suspend fun transcribe(filePath: String): String? = withContext(Dispatchers.IO) {
        try {
            if (modelManager.whisperState.value != ModelState.READY) return@withContext null
            if (recognizer == null) recognizer = buildRecognizer()

            val pcm = M4aToPcmDecoder.decode(filePath, 16000)
            if (pcm.isEmpty()) return@withContext null

            val stream = recognizer!!.createStream()
            stream.acceptWaveform(samples = pcm, sampleRate = 16000)
            recognizer!!.decode(stream)
            val text = recognizer!!.getResult(stream).text.trim()
            stream.release()
            text.ifBlank { null }
        } catch (e: Exception) {
            null
        }
    }
}
