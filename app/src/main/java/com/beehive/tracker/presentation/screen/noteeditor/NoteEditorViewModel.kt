package com.beehive.tracker.presentation.screen.noteeditor

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.domain.usecase.audio.SaveAudioRecordUseCase
import com.beehive.tracker.domain.usecase.note.AddNoteUseCase
import com.beehive.tracker.domain.usecase.tag.GetTagsUseCase
import com.beehive.tracker.presentation.audio.AudioPlayerController
import com.beehive.tracker.presentation.audio.AudioRecorderController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

// NoteEditorScreen'in state kaynağı.
// Ses kaydı state'i ViewModel'de tutulur çünkü kayıt, ekran yeniden çizilse de sürmeli.
@HiltViewModel
class NoteEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
    getTags: GetTagsUseCase,
    private val addNote: AddNoteUseCase,
    private val saveAudioRecord: SaveAudioRecordUseCase,
) : ViewModel() {

    val hiveId: String = checkNotNull(savedStateHandle["hiveId"])

    private val recorder = AudioRecorderController(context.applicationContext)
    private val player = AudioPlayerController()

    val tags: StateFlow<List<Tag>> = getTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Kayıt durumu
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    // Kayıt sayacı (saniye)
    private val _recordingSeconds = MutableStateFlow(0)
    val recordingSeconds: StateFlow<Int> = _recordingSeconds.asStateFlow()

    // Kaydedilmiş dosya bilgileri; null → ses eklenmedi
    private val _savedAudioPath = MutableStateFlow<String?>(null)
    val savedAudioPath: StateFlow<String?> = _savedAudioPath.asStateFlow()

    private val _savedAudioDuration = MutableStateFlow(0)
    val savedAudioDuration: StateFlow<Int> = _savedAudioDuration.asStateFlow()

    // Ses oynatma durumu
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // saveNote() tamamlanınca true → NoteEditorScreen popBackStack yapar
    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private var durationJob: Job? = null

    fun startRecording() {
        if (_isRecording.value) return
        try {
            recorder.start()
            _isRecording.value = true
            _recordingSeconds.value = 0
            durationJob = viewModelScope.launch {
                while (true) {
                    delay(1_000)
                    _recordingSeconds.value++
                }
            }
        } catch (_: Exception) {
            // Mikrofon erişim hatası; kullanıcı izni gelmemiş olabilir
            _isRecording.value = false
        }
    }

    fun stopRecording() {
        if (!_isRecording.value) return
        durationJob?.cancel()
        durationJob = null
        val path = recorder.stop()
        _savedAudioPath.value = path
        _savedAudioDuration.value = _recordingSeconds.value
        _isRecording.value = false
    }

    fun deleteAudio() {
        _savedAudioPath.value?.let { File(it).delete() }
        _savedAudioPath.value = null
        _savedAudioDuration.value = 0
        player.stop()
        _isPlaying.value = false
    }

    fun togglePlayback() {
        val path = _savedAudioPath.value ?: return
        if (_isPlaying.value) {
            player.stop()
            _isPlaying.value = false
        } else {
            player.play(path) { _isPlaying.value = false }
            _isPlaying.value = true
        }
    }

    // Notu ve varsa ses kaydını DB'ye yazar; tamamlanınca _saved = true tetikler navigasyonu.
    fun saveNote(
        tagId: String?,
        tagColorHex: String?,
        tagLabel: String?,
        textContent: String?,
        updateHiveColor: Boolean,
        onSuccess: () -> Unit,
    ) {
        viewModelScope.launch {
            val noteId = UUID.randomUUID().toString()
            addNote(
                hiveId = hiveId,
                tagId = tagId,
                tagColorHex = tagColorHex,
                tagLabel = tagLabel,
                textContent = textContent,
                updateHiveColor = updateHiveColor,
                noteId = noteId,
            )
            _savedAudioPath.value?.let { path ->
                saveAudioRecord(noteId, path, _savedAudioDuration.value)
            }
            _saved.value = true
            onSuccess()
        }
    }

    override fun onCleared() {
        super.onCleared()
        durationJob?.cancel()
        // Kaydedilmemiş veya iptal edilmiş kayıt aktifse temizle
        if (_isRecording.value) recorder.cancel()
        else recorder.release()
        player.release()
        // Not kaydedilmemişse geçici ses dosyasını sil
        if (!_saved.value) {
            _savedAudioPath.value?.let { File(it).delete() }
        }
    }
}
