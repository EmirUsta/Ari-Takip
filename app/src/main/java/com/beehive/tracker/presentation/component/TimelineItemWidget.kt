package com.beehive.tracker.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.beehive.tracker.domain.model.AudioRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Zaman çizelgesindeki tek not kartı.
// Sol tarafta dikey çizgi + daire ile GitHub activity görünümü taklit edilir.
// tagColorHex/tagLabel not yazılırken denormalize kaydedildiğinden
// etiket sonradan silinse bile burada doğru renk ve isim görünür.
// audioRecord null değilse ses oynatıcı widget gösterilir.
@Composable
fun TimelineItemWidget(
    createdAt: Long,
    tagColorHex: String?,
    tagLabel: String?,
    textContent: String?,
    audioRecord: AudioRecord? = null,
    isPlaying: Boolean = false,
    onPlayToggle: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val dateStr = SimpleDateFormat("dd MMM yyyy  HH:mm", Locale("tr")).format(Date(createdAt))
    val dotColor = tagColorHex?.let { parseColor(it) } ?: Color(0xFFBDBDBD)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        // Zaman çizelgesi sol sütunu: dikey çizgi + renkli nokta
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(24.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(dotColor, CircleShape)
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(if (audioRecord != null) 72.dp else 48.dp)
                    .background(Color(0xFFE0E0E0))
            )
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
                Text(
                    text = textContent,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (audioRecord != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AudioPlayerWidget(
                    durationSeconds = audioRecord.durationSeconds,
                    isPlaying = isPlaying,
                    onPlayToggle = onPlayToggle,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
