package com.beehive.tracker.data.stt

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

enum class ModelState { NOT_DOWNLOADED, DOWNLOADING, READY, ERROR }

@Singleton
class ModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val modelsDir = File(context.filesDir, "stt_models")

    private val _voskState = MutableStateFlow(ModelState.NOT_DOWNLOADED)
    val voskState: StateFlow<ModelState> = _voskState.asStateFlow()

    private val _whisperState = MutableStateFlow(ModelState.NOT_DOWNLOADED)
    val whisperState: StateFlow<ModelState> = _whisperState.asStateFlow()

    val voskModelPath: String get() = File(modelsDir, "vosk-model-small-tr-0.3").absolutePath
    val whisperModelDir: String get() = File(modelsDir, "sherpa-whisper-tiny").absolutePath

    init {
        modelsDir.mkdirs()
        if (File(voskModelPath).exists()) _voskState.value = ModelState.READY
        if (whisperFilesExist()) _whisperState.value = ModelState.READY
    }

    private fun whisperFilesExist(): Boolean {
        val dir = File(whisperModelDir)
        return dir.exists()
            && File(dir, "tiny-encoder.int8.onnx").exists()
            && File(dir, "tiny-decoder.int8.onnx").exists()
            && File(dir, "tiny-tokens.txt").exists()
    }

    suspend fun downloadVosk() = withContext(Dispatchers.IO) {
        if (_voskState.value == ModelState.READY) return@withContext
        _voskState.value = ModelState.DOWNLOADING
        try {
            val zipFile = File(modelsDir, "vosk-tr.zip")
            URL("https://alphacephei.com/vosk/models/vosk-model-small-tr-0.3.zip")
                .openStream().use { input -> zipFile.outputStream().use { input.copyTo(it) } }
            unzip(zipFile, modelsDir)
            zipFile.delete()
            _voskState.value = ModelState.READY
        } catch (e: Exception) {
            _voskState.value = ModelState.ERROR
        }
    }

    suspend fun downloadWhisper() = withContext(Dispatchers.IO) {
        if (_whisperState.value == ModelState.READY) return@withContext
        _whisperState.value = ModelState.DOWNLOADING
        try {
            val dir = File(whisperModelDir).also { it.mkdirs() }
            val base = "https://huggingface.co/csukuangfj/sherpa-onnx-whisper-tiny/resolve/main"
            listOf(
                "tiny-encoder.int8.onnx",
                "tiny-decoder.int8.onnx",
                "tiny-tokens.txt",
            ).forEach { name ->
                URL("$base/$name").openStream()
                    .use { input -> File(dir, name).outputStream().use { input.copyTo(it) } }
            }
            _whisperState.value = ModelState.READY
        } catch (e: Exception) {
            _whisperState.value = ModelState.ERROR
        }
    }

    private fun unzip(zip: File, targetDir: File) {
        ZipInputStream(zip.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val outFile = File(targetDir, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    outFile.outputStream().use { zis.copyTo(it) }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
    }
}
