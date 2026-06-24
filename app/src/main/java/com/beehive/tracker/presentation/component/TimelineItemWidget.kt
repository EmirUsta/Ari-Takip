package com.beehive.tracker.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.beehive.tracker.data.stt.ModelState
import com.beehive.tracker.domain.model.AudioRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimelineItemWidget(
    createdAt: Long,
    tagColorHex: String?,
    tagLabel: String?,
    textContent: String?,
    audioRecord: AudioRecord? = null,
    isPlaying: Boolean = false,
    isProcessing: Boolean = false,
    voskModelState: ModelState = ModelState.NOT_READY,
    whisperModelState: ModelState = ModelState.NOT_READY,
    onPlayToggle: () -> Unit = {},
    onTranscribeVosk: () -> Unit = {},
    onTranscribeWhisper: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale("tr")).format(Date(createdAt))
    val dotColor = tagColorHex?.let { parseColor(it) } ?: Color(0xFFBDBDBD)

    val hasAudio = audioRecord != null
    val hasTranscription = hasAudio &&
        (!audioRecord!!.transcriptionVosk.isNullOrBlank() || !audioRecord.transcriptionWhisper.isNullOrBlank())
    val lineHeight = when {
        hasTranscription -> 120.dp
        hasAudio -> 72.dp
        else -> 48.dp
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp),
        ) {
            Box(modifier = Modifier.size(14.dp).background(dotColor, CircleShape))
            Box(modifier = Modifier.width(2.dp).height(lineHeight).background(Color(0xFFE0E0E0)))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            )

            if (tagLabel != null && tagColorHex != null) {
                Spacer(modifier = Modifier.height(4.dp))
                TagChip(label = tagLabel, colorHex = tagColorHex)
            }

            if (!textContent.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = textContent, style = MaterialTheme.typography.bodyMedium)
            }

            if (audioRecord != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AudioPlayerWidget(
                    durationSeconds = audioRecord.durationSeconds,
                    isPlaying = isPlaying,
                    onPlayToggle = onPlayToggle,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (isProcessing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("İşleniyor…", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SttButton(
                            label = when (voskModelState) {
                                ModelState.COPYING -> "Vosk Hazırlanıyor…"
                                else -> "Vosk"
                            },
                            enabled = voskModelState != ModelState.COPYING,
                            onClick = onTranscribeVosk,
                        )
                        SttButton(
                            label = when (whisperModelState) {
                                ModelState.COPYING -> "Whisper Hazırlanıyor…"
                                else -> "Whisper"
                            },
                            enabled = whisperModelState != ModelState.COPYING,
                            onClick = onTranscribeWhisper,
                        )
                    }
                }

                if (!audioRecord.transcriptionWhisper.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TranscriptCard(label = "Whisper", text = audioRecord.transcriptionWhisper)
                }
                if (!audioRecord.transcriptionVosk.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TranscriptCard(label = "Vosk", text = audioRecord.transcriptionVosk)
                }
            }
        }
    }
}

@Composable
private fun SttButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TranscriptCard(label: String, text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = text, style = MaterialTheme.typography.bodySmall)
        }
    }
}
