package com.beehive.tracker.data.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.beehive.tracker.data.preferences.AudioPreferencesDataSource
import com.beehive.tracker.domain.usecase.audio.PurgeOldAudioUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class AudioPurgeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val purgeOldAudio: PurgeOldAudioUseCase,
    private val audioPrefs: AudioPreferencesDataSource,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val ttlDays = audioPrefs.purgeTtlDays.first()
        purgeOldAudio(ttlDays)
        return Result.success()
    }
}
