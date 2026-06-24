package com.beehive.tracker.data.stt

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteOrder

object M4aToPcmDecoder {

    fun decode(filePath: String, targetSampleRate: Int = 16000): FloatArray {
        val extractor = MediaExtractor()
        extractor.setDataSource(filePath)

        var trackIndex = -1
        var format: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val f = extractor.getTrackFormat(i)
            if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                trackIndex = i
                format = f
                break
            }
        }
        if (trackIndex < 0 || format == null) {
            extractor.release()
            return FloatArray(0)
        }

        extractor.selectTrack(trackIndex)
        val mime = format.getString(MediaFormat.KEY_MIME)!!
        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val bufferInfo = MediaCodec.BufferInfo()
        val pcmShorts = mutableListOf<Short>()
        var inputDone = false
        var outputDone = false

        while (!outputDone) {
            if (!inputDone) {
                val inputIdx = codec.dequeueInputBuffer(10_000)
                if (inputIdx >= 0) {
                    val buf = codec.getInputBuffer(inputIdx)!!
                    val size = extractor.readSampleData(buf, 0)
                    if (size < 0) {
                        codec.queueInputBuffer(inputIdx, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                        inputDone = true
                    } else {
                        codec.queueInputBuffer(inputIdx, 0, size, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                }
            }

            val outputIdx = codec.dequeueOutputBuffer(bufferInfo, 10_000)
            if (outputIdx >= 0) {
                val buf = codec.getOutputBuffer(outputIdx)!!
                val shortBuf = buf.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()
                val chunk = ShortArray(shortBuf.remaining())
                shortBuf.get(chunk)
                pcmShorts.addAll(chunk.asIterable())
                codec.releaseOutputBuffer(outputIdx, false)
                if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
            }
        }

        codec.stop()
        codec.release()

        val sourceSampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)

        val mono: ShortArray = if (channelCount > 1) {
            ShortArray(pcmShorts.size / channelCount) { i ->
                (0 until channelCount).sumOf { ch -> pcmShorts[i * channelCount + ch].toInt() }
                    .div(channelCount).toShort()
            }
        } else {
            pcmShorts.toShortArray()
        }

        val resampled = if (sourceSampleRate != targetSampleRate) {
            resample(mono, sourceSampleRate, targetSampleRate)
        } else {
            mono
        }

        extractor.release()
        return FloatArray(resampled.size) { i -> resampled[i] / 32768.0f }
    }

    private fun resample(input: ShortArray, fromRate: Int, toRate: Int): ShortArray {
        val ratio = fromRate.toDouble() / toRate
        val outSize = (input.size / ratio).toInt()
        return ShortArray(outSize) { i ->
            val pos = i * ratio
            val idx = pos.toInt().coerceIn(0, input.size - 2)
            val frac = pos - idx
            (input[idx] * (1.0 - frac) + input[idx + 1] * frac).toInt().toShort()
        }
    }
}
