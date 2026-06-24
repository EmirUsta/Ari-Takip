package com.beehive.tracker.data.stt

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

enum class ModelState { NOT_READY, COPYING, READY, ERROR }

@Singleton
class ModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val modelsDir = File(context.filesDir, "stt_models")

    private val _voskState = MutableStateFlow(ModelState.NOT_READY)
    val voskState: StateFlow<ModelState> = _voskState.asStateFlow()

    private val _whisperState = MutableStateFlow(ModelState.NOT_READY)
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

    suspend fun ensureVosk() = withContext(Dispatchers.IO) {
        if (_voskState.value == ModelState.READY) return@withContext
        _voskState.value = ModelState.COPYING
        try {
            copyAssetDir("stt_models/vosk-model-small-tr-0.3", File(voskModelPath))
            _voskState.value = ModelState.READY
        } catch (e: Exception) {
            _voskState.value = ModelState.ERROR
        }
    }

    suspend fun ensureWhisper() = withContext(Dispatchers.IO) {
        if (_whisperState.value == ModelState.READY) return@withContext
        _whisperState.value = ModelState.COPYING
        try {
            val dir = File(whisperModelDir).also { it.mkdirs() }
            listOf("tiny-encoder.int8.onnx", "tiny-decoder.int8.onnx", "tiny-tokens.txt")
                .forEach { name ->
                    context.assets.open("stt_models/sherpa-whisper-tiny/$name")
                        .use { src -> File(dir, name).outputStream().use { src.copyTo(it) } }
                }
            _whisperState.value = ModelState.READY
        } catch (e: Exception) {
            _whisperState.value = ModelState.ERROR
        }
    }

    private fun copyAssetDir(assetPath: String, targetDir: File) {
        val list = context.assets.list(assetPath) ?: return
        if (list.isEmpty()) {
            targetDir.parentFile?.mkdirs()
            context.assets.open(assetPath).use { src ->
                targetDir.outputStream().use { src.copyTo(it) }
            }
        } else {
            targetDir.mkdirs()
            list.forEach { child ->
                copyAssetDir("$assetPath/$child", File(targetDir, child))
            }
        }
    }
}
