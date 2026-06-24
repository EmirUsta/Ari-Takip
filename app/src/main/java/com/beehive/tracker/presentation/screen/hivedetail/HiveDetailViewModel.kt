package com.beehive.tracker.presentation.screen.hivedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.model.NoteWithAudio
import com.beehive.tracker.domain.repository.HiveRepository
import com.beehive.tracker.domain.usecase.note.GetNotesWithAudioUseCase
import com.beehive.tracker.presentation.audio.AudioPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

// HiveDetailScreen'in state kaynağı.
// notes: NoteWithAudio listesi — zaman çizelgesi + ses oynatıcı için.
// Ses oynatma state'i burada tutulur; aynı anda yalnızca bir kayıt çalabilir.
@HiltViewModel
class HiveDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hiveRepository: HiveRepository,
    getNotesWithAudio: GetNotesWithAudioUseCase,
) : ViewModel() {

    val hiveId: String = checkNotNull(savedStateHandle["hiveId"])

    val hive: StateFlow<Hive?> = hiveRepository.observeById(hiveId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Her note.note.id için oynatma durumu; sadece biri true olabilir
    private val _playingNoteId = MutableStateFlow<String?>(null)
    val playingNoteId: StateFlow<String?> = _playingNoteId.asStateFlow()

    val notes: StateFlow<List<NoteWithAudio>> = getNotesWithAudio(hiveId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val player = AudioPlayerController()

    fun togglePlayback(noteId: String, filePath: String) {
        if (_playingNoteId.value == noteId) {
            player.stop()
            _playingNoteId.value = null
        } else {
            player.play(filePath) { _playingNoteId.value = null }
            _playingNoteId.value = noteId
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
