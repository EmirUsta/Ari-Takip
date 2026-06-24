package com.beehive.tracker

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.beehive.tracker.data.workers.AudioPurgeWorker
import com.beehive.tracker.domain.usecase.tag.SeedDefaultTagsUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class BeeHiveApplication : Application(), Configuration.Provider {

    @Inject lateinit var seedDefaultTags: SeedDefaultTagsUseCase
    @Inject lateinit var workerFactory: HiltWorkerFactory

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        appScope.launch { seedDefaultTags() }
        scheduleAudioPurge()
    }

    private fun scheduleAudioPurge() {
        val request = PeriodicWorkRequestBuilder<AudioPurgeWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "audio_purge",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
