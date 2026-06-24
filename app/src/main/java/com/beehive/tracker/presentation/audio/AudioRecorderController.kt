package com.beehive.tracker.presentation.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.util.UUID

// MediaRecorder sarmalayıcı. NoteEditorViewModel bu sınıfı tutar.
// Kaydı başlatır, durdurur ve iptal eder.
// AAC/MPEG_4 formatı: küçük dosya boyutu, iyi kalite, Android 5.0+ tam destek.
class AudioRecorderController(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentPath: String? = null

    // Kaydı başlatır; dönen String kaydedilen dosyanın mutlak yoludur.
    fun start(): String {
        val dir = File(context.filesDir, "audio").also { it.mkdirs() }
        val file = File(dir, "${UUID.randomUUID()}.m4a")
        currentPath = file.absolutePath

        @Suppress("DEPRECATION")
        recorder = (
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                MediaRecorder(context)
            else
                MediaRecorder()
        ).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(currentPath)
            prepare()
            start()
        }
        return currentPath!!
    }

    // Kaydı durdurur; dosya yolunu döner.
    fun stop(): String? {
        recorder?.apply { stop(); release() }
        recorder = null
        return currentPath
    }

    // Kaydı iptal eder; yarım kalan dosyayı siler.
    fun cancel() {
        recorder?.apply { stop(); release() }
        recorder = null
        currentPath?.let { File(it).delete() }
        currentPath = null
    }

    fun release() {
        recorder?.release()
        recorder = null
    }
}
