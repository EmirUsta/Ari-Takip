package com.beehive.tracker.presentation.screen.noteeditor

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beehive.tracker.domain.model.Tag
import com.beehive.tracker.presentation.component.AudioPlayerWidget
import com.beehive.tracker.presentation.component.TagChip
import com.beehive.tracker.presentation.component.parseColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = hiltViewModel(),
) {
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val recordingSeconds by viewModel.recordingSeconds.collectAsStateWithLifecycle()
    val savedAudioPath by viewModel.savedAudioPath.collectAsStateWithLifecycle()
    val savedAudioDuration by viewModel.savedAudioDuration.collectAsStateWithLifecycle()
    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()

    var selectedTag by remember { mutableStateOf<Tag?>(null) }
    var textContent by remember { mutableStateOf("") }
    var updateHiveColor by remember { mutableStateOf(true) }

    val context = LocalContext.current

    // RECORD_AUDIO izin başlatıcı
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startRecording()
    }

    // İzin kontrolü ile kaydı başlat
    fun onMicClick() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startRecording()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Not") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (textContent.isNotBlank() || selectedTag != null || savedAudioPath != null) {
                                viewModel.saveNote(
                                    tagId = selectedTag?.id,
                                    tagColorHex = selectedTag?.colorHex,
                                    tagLabel = selectedTag?.label,
                                    textContent = textContent.ifBlank { null },
                                    updateHiveColor = updateHiveColor,
                                    onSuccess = onBack,
                                )
                            }
                        }
                    ) { Text("Kaydet") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {

            // --- ETİKET SEÇİCİ ---
            Text("Durum Etiketi", style = MaterialTheme.typography.titleSmall)

            if (tags.isEmpty()) {
                Text(
                    "Etiket yok — Harita > Ayarlar'dan ekle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tags, key = { it.id }) { tag ->
                        val isSelected = tag.id == selectedTag?.id
                        Box(
                            modifier = Modifier
                                .clickable { selectedTag = if (isSelected) null else tag }
                                .then(
                                    if (isSelected) Modifier.border(
                                        2.dp,
                                        parseColor(tag.colorHex),
                                        RoundedCornerShape(50),
                                    ) else Modifier
                                )
                                .padding(2.dp),
                        ) {
                            TagChip(label = tag.label, colorHex = tag.colorHex)
                        }
                    }
                }
            }

            HorizontalDivider()

            // --- SES KAYDI ---
            Text("Sesli Not", style = MaterialTheme.typography.titleSmall)

            when {
                // Kayıt devam ediyor
                isRecording -> {
                    val timer = "%d:%02d".format(recordingSeconds / 60, recordingSeconds % 60)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.stopRecording() },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Durdur")
                            Spacer(Modifier.width(4.dp))
                            Text("Durdur  $timer")
                        }
                    }
                }

                // Ses kaydedilmiş — oynat veya sil
                savedAudioPath != null -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        AudioPlayerWidget(
                            durationSeconds = savedAudioDuration,
                            isPlaying = isPlaying,
                            onPlayToggle = { viewModel.togglePlayback() },
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = { viewModel.deleteAudio() }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sesi sil",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }

                // Henüz ses yok — kayıt başlat
                else -> {
                    OutlinedButton(onClick = { onMicClick() }) {
                        Icon(Icons.Default.Mic, contentDescription = "Mikrofon")
                        Spacer(Modifier.width(6.dp))
                        Text("Ses Kaydı Başlat")
                    }
                }
            }

            HorizontalDivider()

            // --- YAZI ALANI ---
            Text("Not", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                value = textContent,
                onValueChange = { textContent = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                placeholder = { Text("Arıların durumu, yapılan işlem, dikkat edilecekler…") },
                maxLines = 10,
            )

            HorizontalDivider()

            // --- KOVAN RENGİ GÜNCELLEME TOGGLE ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Kovan rengini güncelle", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Bu notun etiketi haritada görünsün",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
                Switch(
                    checked = updateHiveColor,
                    onCheckedChange = { updateHiveColor = it },
                    enabled = selectedTag != null,
                )
            }
        }
    }
}
