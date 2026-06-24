package com.beehive.tracker.core.di

import android.content.Context
import androidx.room.Room
import com.beehive.tracker.data.local.AppDatabase
import com.beehive.tracker.data.local.MIGRATION_1_2
import com.beehive.tracker.data.local.MIGRATION_2_3
import com.beehive.tracker.data.local.MIGRATION_3_4
import com.beehive.tracker.data.local.dao.ApiaryDao
import com.beehive.tracker.data.local.dao.AudioRecordDao
import com.beehive.tracker.data.local.dao.HiveDao
import com.beehive.tracker.data.local.dao.NoteDao
import com.beehive.tracker.data.local.dao.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Room veritabanı ve tüm DAO'ları Hilt grafiğine bağlar.
// Yeni faz eklenince sadece yeni Migration nesnesi addMigrations'a eklenir.
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "kovan_takip.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

    @Provides fun provideApiaryDao(db: AppDatabase): ApiaryDao = db.apiaryDao()
    @Provides fun provideHiveDao(db: AppDatabase): HiveDao = db.hiveDao()
    @Provides fun provideTagDao(db: AppDatabase): TagDao = db.tagDao()
    @Provides fun provideNoteDao(db: AppDatabase): NoteDao = db.noteDao()
    @Provides fun provideAudioRecordDao(db: AppDatabase): AudioRecordDao = db.audioRecordDao()
}
