package com.beehive.tracker.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Zaman çizelgesindeki ses kaydı oynatıcı.
// filePath yerel dosya; durationSeconds toplam süre.
// isPlaying dışarıdan kontrol edilir; onPlayToggle ViewModel'e iletilir.
@Composable
fun AudioPlayerWidget(
    durationSeconds: Int,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val durationLabel = "%d:%02d".format(durationSeconds / 60, durationSeconds % 60)

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            // Oynat / Duraklat butonu
            IconButton(
                onClick = onPlayToggle,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Duraklat" else "Oynat",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Dalga formu yerine statik çubuklar (STT hazır olunca transkript buraya gelir)
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
            ) {
                val heights = listOf(12, 20, 16, 24, 18, 14, 22, 10, 20, 16)
                heights.forEach { h ->
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(h.dp)
                            .background(
                                if (isPlaying) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(2.dp),
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = durationLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
