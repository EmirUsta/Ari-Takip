package com.beehive.tracker.core.di

import com.beehive.tracker.data.remote.NoOpTranscriptionDataSource
import com.beehive.tracker.data.remote.TranscriptionRemoteDataSource
import com.beehive.tracker.data.repository.ApiaryRepositoryImpl
import com.beehive.tracker.data.repository.AudioRepositoryImpl
import com.beehive.tracker.data.repository.HiveRepositoryImpl
import com.beehive.tracker.data.repository.NoteRepositoryImpl
import com.beehive.tracker.data.repository.TagRepositoryImpl
import com.beehive.tracker.domain.repository.ApiaryRepository
import com.beehive.tracker.domain.repository.AudioRepository
import com.beehive.tracker.domain.repository.HiveRepository
import com.beehive.tracker.domain.repository.NoteRepository
import com.beehive.tracker.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Domain interface'lerini Data implementasyonlarına bağlar.
// @Binds yeni nesne üretmez; Hilt sadece tipi eşler — @Provides'tan daha verimlidir.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindApiaryRepository(impl: ApiaryRepositoryImpl): ApiaryRepository

    @Binds @Singleton
    abstract fun bindHiveRepository(impl: HiveRepositoryImpl): HiveRepository

    @Binds @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository

    @Binds @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds @Singleton
    abstract fun bindAudioRepository(impl: AudioRepositoryImpl): AudioRepository

    // STT servisi hazır olunca NoOp yerine gerçek implementasyon buraya bağlanır
    @Binds @Singleton
    abstract fun bindTranscriptionDataSource(impl: NoOpTranscriptionDataSource): TranscriptionRemoteDataSource
}
