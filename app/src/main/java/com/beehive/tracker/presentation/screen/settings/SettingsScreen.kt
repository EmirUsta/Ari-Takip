package com.beehive.tracker.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beehive.tracker.domain.model.GridPrefs
import com.beehive.tracker.presentation.component.TagChip
import com.beehive.tracker.presentation.component.parseColor

private val PRESET_COLORS = listOf(
    "#E53935", "#43A047", "#FDD835", "#1E88E5", "#FB8C00",
    "#8E24AA", "#00897B", "#F06292", "#795548", "#546E7A",
    "#D81B60", "#00ACC1", "#C0CA33", "#6D4C41", "#26A69A",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val gridPrefs by viewModel.gridPreferences.collectAsStateWithLifecycle()
    var showDialog by remember { mutableStateOf(false) }
    var newLabel by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PRESET_COLORS.first()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Yeni etiket")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            GridSettingsSection(
                prefs = gridPrefs,
                onColsChange = { viewModel.setCols(it) },
                onRowsChange = { viewModel.setRows(it) },
                onGapChange  = { viewModel.setGap(it) },
            )

            HorizontalDivider()

            Text(
                text = "Etiket Yönetimi",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (tags.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Henüz etiket yok.\n+ ile ekle.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    items(tags, key = { it.id }) { tag ->
                        ListItem(
                            headlineContent = {
                                TagChip(label = tag.label, colorHex = tag.colorHex)
                            },
                            trailingContent = {
                                IconButton(onClick = { viewModel.removeTag(tag.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Sil",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false; newLabel = ""; selectedColor = PRESET_COLORS.first() },
            title = { Text("Yeni Etiket") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        label = { Text("Etiket adı") },
                        singleLine = true,
                    )
                    Text("Renk seç", style = MaterialTheme.typography.labelLarge)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(PRESET_COLORS) { hex ->
                            val color = parseColor(hex)
                            val isSelected = hex == selectedColor
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .then(
                                        if (isSelected) Modifier.border(3.dp, Color.White, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { selectedColor = hex },
                            )
                        }
                    }
                    TagChip(label = newLabel.ifBlank { "Önizleme" }, colorHex = selectedColor)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newLabel.isNotBlank()) {
                            viewModel.addTag(newLabel.trim(), selectedColor)
                            showDialog = false
                            newLabel = ""
                            selectedColor = PRESET_COLORS.first()
                        }
                    }
                ) { Text("Ekle") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false; newLabel = ""; selectedColor = PRESET_COLORS.first()
                }) { Text("İptal") }
            },
        )
    }
}

@Composable
private fun GridSettingsSection(
    prefs: GridPrefs,
    onColsChange: (Int) -> Unit,
    onRowsChange: (Int) -> Unit,
    onGapChange: (Int) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = "Izgara Boyutu (Sütun × Satır)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 4.dp),
        )
        StepRow(
            label = "Sütun (A)",
            value = prefs.cols,
            unit = "sütun",
            onDecrease = { onColsChange(prefs.cols - 1) },
            onIncrease = { onColsChange(prefs.cols + 1) },
            decreaseEnabled = prefs.cols > 2,
            increaseEnabled = prefs.cols < 20,
        )
        StepRow(
            label = "Satır (B)",
            value = prefs.rows,
            unit = "satır",
            onDecrease = { onRowsChange(prefs.rows - 1) },
            onIncrease = { onRowsChange(prefs.rows + 1) },
            decreaseEnabled = prefs.rows > 2,
            increaseEnabled = prefs.rows < 50,
        )
        StepRow(
            label = "Boşluk",
            value = prefs.gap,
            unit = "dp",
            onDecrease = { onGapChange(prefs.gap - 4) },
            onIncrease = { onGapChange(prefs.gap + 4) },
            decreaseEnabled = prefs.gap > 0,
            increaseEnabled = prefs.gap < 20,
        )
        Text(
            text = "Ayar değişince haritada 'Izgara Düzenle' butonuna basın.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun StepRow(
    label: String,
    value: Int,
    unit: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDecrease, enabled = decreaseEnabled) {
            Text("−", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.widthIn(min = 80.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(onClick = onIncrease, enabled = increaseEnabled) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}
