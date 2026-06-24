package com.beehive.tracker

import android.app.Application
import com.beehive.tracker.domain.usecase.tag.SeedDefaultTagsUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BeeHiveApplication : Application() {

    @Inject lateinit var seedDefaultTags: SeedDefaultTagsUseCase

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appScope.launch { seedDefaultTags() }
    }
}
