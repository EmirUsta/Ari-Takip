package com.beehive.tracker.presentation.screen.hivedetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beehive.tracker.presentation.component.TimelineItemWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiveDetailScreen(
    hiveId: String,
    onBack: () -> Unit,
    onAddNote: (hiveId: String) -> Unit,
    viewModel: HiveDetailViewModel = hiltViewModel(),
) {
    val hive by viewModel.hive.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val playingNoteId by viewModel.playingNoteId.collectAsStateWithLifecycle()
    val processingIds by viewModel.processingIds.collectAsStateWithLifecycle()
    val voskModelState by viewModel.voskModelState.collectAsStateWithLifecycle()
    val whisperModelState by viewModel.whisperModelState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(hive?.name ?: "Kovan Detayı") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddNote(hiveId) }) {
                Icon(Icons.Default.Add, contentDescription = "Not ekle")
            }
        },
    ) { padding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Henüz not yok.\n+ ile ilk notu ekle.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                items(notes, key = { it.note.id }) { noteWithAudio ->
                    val audio = noteWithAudio.audioRecord
                    TimelineItemWidget(
                        createdAt = noteWithAudio.note.createdAt,
                        tagColorHex = noteWithAudio.note.tagColorHex,
                        tagLabel = noteWithAudio.note.tagLabel,
                        textContent = noteWithAudio.note.textContent,
                        audioRecord = audio,
                        isPlaying = playingNoteId == noteWithAudio.note.id,
                        isProcessing = audio != null && processingIds.contains(audio.id),
                        voskModelState = voskModelState,
                        whisperModelState = whisperModelState,
                        onPlayToggle = {
                            audio?.let { viewModel.togglePlayback(noteWithAudio.note.id, it.filePath) }
                        },
                        onTranscribeVosk = {
                            audio?.let { viewModel.transcribeWithVosk(it.id, it.filePath) }
                        },
                        onTranscribeWhisper = {
                            audio?.let { viewModel.transcribeWithWhisper(it.id, it.filePath) }
                        },
                    )
                }
            }
        }
    }
}
