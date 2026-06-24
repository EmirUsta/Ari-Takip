package com.beehive.tracker.presentation.screen.hivedetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beehive.tracker.data.stt.ModelManager
import com.beehive.tracker.data.stt.ModelState
import com.beehive.tracker.data.stt.SherpaWhisperSttEngine
import com.beehive.tracker.data.stt.VoskSttEngine
import com.beehive.tracker.domain.model.Hive
import com.beehive.tracker.domain.model.NoteWithAudio
import com.beehive.tracker.domain.repository.AudioRepository
import com.beehive.tracker.domain.repository.HiveRepository
import com.beehive.tracker.domain.usecase.note.GetNotesWithAudioUseCase
import com.beehive.tracker.presentation.audio.AudioPlayerController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HiveDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val hiveRepository: HiveRepository,
    private val audioRepository: AudioRepository,
    private val modelManager: ModelManager,
    private val voskEngine: VoskSttEngine,
    private val whisperEngine: SherpaWhisperSttEngine,
    getNotesWithAudio: GetNotesWithAudioUseCase,
) : ViewModel() {

    val hiveId: String = checkNotNull(savedStateHandle["hiveId"])

    val hive: StateFlow<Hive?> = hiveRepository.observeById(hiveId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val notes: StateFlow<List<NoteWithAudio>> = getNotesWithAudio(hiveId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _playingNoteId = MutableStateFlow<String?>(null)
    val playingNoteId: StateFlow<String?> = _playingNoteId.asStateFlow()

    // Hangi audioId'ler şu an işleniyor
    private val _processingIds = MutableStateFlow<Set<String>>(emptySet())
    val processingIds: StateFlow<Set<String>> = _processingIds.asStateFlow()

    val voskModelState: StateFlow<ModelState> = modelManager.voskState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModelState.NOT_READY)

    val whisperModelState: StateFlow<ModelState> = modelManager.whisperState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ModelState.NOT_READY)

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

    fun transcribeWithVosk(audioId: String, filePath: String) {
        viewModelScope.launch {
            if (modelManager.voskState.value != ModelState.READY) {
                modelManager.ensureVosk()
            }
            if (modelManager.voskState.value != ModelState.READY) return@launch
            _processingIds.value = _processingIds.value + audioId
            val text = voskEngine.transcribe(filePath)
            audioRepository.updateVosk(audioId, text)
            _processingIds.value = _processingIds.value - audioId
        }
    }

    fun transcribeWithWhisper(audioId: String, filePath: String) {
        viewModelScope.launch {
            if (modelManager.whisperState.value != ModelState.READY) {
                modelManager.ensureWhisper()
            }
            if (modelManager.whisperState.value != ModelState.READY) return@launch
            _processingIds.value = _processingIds.value + audioId
            val text = whisperEngine.transcribe(filePath)
            audioRepository.updateWhisper(audioId, text)
            _processingIds.value = _processingIds.value - audioId
        }
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }
}
