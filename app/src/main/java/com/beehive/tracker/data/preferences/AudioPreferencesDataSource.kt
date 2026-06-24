package com.beehive.tracker.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.audioDataStore: DataStore<Preferences> by preferencesDataStore("audio_settings")

@Singleton
class AudioPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val purgeTtlDaysKey = intPreferencesKey("purge_ttl_days")

    // 0 = hiçbir zaman silme
    val purgeTtlDays: Flow<Int> = context.audioDataStore.data.map { prefs ->
        prefs[purgeTtlDaysKey] ?: 0
    }

    suspend fun setPurgeTtlDays(days: Int) {
        context.audioDataStore.edit { it[purgeTtlDaysKey] = days.coerceIn(0, 365) }
    }
}
