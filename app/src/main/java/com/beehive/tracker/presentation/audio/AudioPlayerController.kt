package com.beehive.tracker.presentation.audio

import android.media.MediaPlayer

// MediaPlayer sarmalayıcı. Yerel dosya yolundan ses oynatır.
// play() çağrılınca önceki oynatma otomatik durdurulur.
class AudioPlayerController {

    private var player: MediaPlayer? = null

    fun play(filePath: String, onComplete: () -> Unit) {
        player?.release()
        player = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            setOnCompletionListener { onComplete() }
            start()
        }
    }

    fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

    fun release() {
        player?.release()
        player = null
    }
}
