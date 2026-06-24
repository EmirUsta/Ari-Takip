package com.beehive.tracker.presentation.screen.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beehive.tracker.core.constants.GridConstants
import com.beehive.tracker.domain.model.GridPrefs
import com.beehive.tracker.presentation.component.HivePinWidget
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    apiaryId: String,
    onHiveClick: (hiveId: String) -> Unit,
    onBack: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val hives by viewModel.hives.collectAsStateWithLifecycle()
    val isEditMode by viewModel.isEditMode.collectAsStateWithLifecycle()
    val gridPrefs by viewModel.gridPrefs.collectAsStateWithLifecycle()

    // Density-aware hesaplama: kovan 72dp, hücre = 72dp + gap
    val density = LocalDensity.current
    val hivePx  = with(density) { 72.dp.toPx() }
    val gapPx   = with(density) { gridPrefs.gap.dp.toPx() }
    val cellPx  = hivePx + gapPx
    val halfGap = gapPx / 2f
    val sx = GridConstants.START_X
    val sy = GridConstants.START_Y

    // Slot snap: hücre dışına çıkılamaz; kovan hücre içinde ortalar (halfGap offset)
    val snapFn: (Offset) -> Offset = { raw ->
        val col = ((raw.x - sx - halfGap) / cellPx).roundToInt().coerceIn(0, gridPrefs.cols - 1)
        val row = ((raw.y - sy - halfGap) / cellPx).roundToInt().coerceIn(0, gridPrefs.rows - 1)
        Offset(sx + col * cellPx + halfGap, sy + row * cellPx + halfGap)
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var tapOffset by remember(gridPrefs, cellPx, halfGap) {
        mutableStateOf(Offset(sx + halfGap, sy + halfGap))
    }
    var newHiveName by remember { mutableStateOf("") }

    var mapScale by remember { mutableFloatStateOf(1f) }
    var mapOffset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        mapScale = (mapScale * zoomChange).coerceIn(0.3f, 4f)
        mapOffset += panChange
    }

    // Grid renkleri: edit modda canlı amber, view modda soluk
    val fillColor  = if (isEditMode) Color(0x22B8860B) else Color(0x08B8860B)
    val lineColor  = if (isEditMode) Color(0x55B8860B) else Color(0x18B8860B)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Harita")
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(50),
                        ) {
                            Text(
                                text = "${gridPrefs.cols}×${gridPrefs.rows}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            )
                        }
                        if (hives.isNotEmpty()) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(50),
                            ) {
                                Text(
                                    text = "${hives.size} kovan",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                actions = {
                    if (hives.isNotEmpty()) {
                        IconButton(onClick = { viewModel.arrangeGrid(cellPx, halfGap) }) {
                            Icon(Icons.Default.GridView, "Izgara düzenle")
                        }
                    }
                    IconButton(onClick = { viewModel.toggleEditMode() }) {
                        Icon(
                            imageVector = if (isEditMode) Icons.Default.Lock else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "Görüntüleme modu" else "Düzenleme modu",
                            tint = if (isEditMode) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                        )
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Etiketler")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val (x, y) = viewModel.firstEmptySlot(cellPx, halfGap)
                tapOffset = Offset(x, y)
                showAddDialog = true
            }) {
                Icon(Icons.Default.Add, "Yeni kovan")
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F0E8))
                .transformable(state = transformState, enabled = !isEditMode)
                .pointerInput(isEditMode, snapFn) {
                    if (isEditMode) {
                        detectTapGestures(
                            onLongPress = { offset ->
                                val raw = Offset(
                                    (offset.x - mapOffset.x) / mapScale,
                                    (offset.y - mapOffset.y) / mapScale,
                                )
                                tapOffset = snapFn(raw)
                                showAddDialog = true
                            }
                        )
                    }
                },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = mapScale,
                        scaleY = mapScale,
                        translationX = mapOffset.x,
                        translationY = mapOffset.y,
                    ),
            ) {
                // Grid her zaman çizilir; tam AxB hücreden ibaret
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cols = gridPrefs.cols
                    val rows = gridPrefs.rows

                    // Hücre dolguları
                    for (r in 0 until rows) {
                        for (c in 0 until cols) {
                            drawRect(
                                color = fillColor,
                                topLeft = Offset(sx + c * cellPx, sy + r * cellPx),
                                size = Size(cellPx, cellPx),
                            )
                        }
                    }
                    // Grid sınır çizgileri (yalnızca AxB alanında)
                    val gridW = cols * cellPx
                    val gridH = rows * cellPx
                    for (c in 0..cols) {
                        val x = sx + c * cellPx
                        drawLine(lineColor, Offset(x, sy), Offset(x, sy + gridH), strokeWidth = 1.5f)
                    }
                    for (r in 0..rows) {
                        val y = sy + r * cellPx
                        drawLine(lineColor, Offset(sx, y), Offset(sx + gridW, y), strokeWidth = 1.5f)
                    }
                }

                hives.forEach { hive ->
                    HivePinWidget(
                        name = hive.name,
                        posX = hive.posX,
                        posY = hive.posY,
                        colorHex = hive.currentTagColorHex,
                        isEditMode = isEditMode,
                        snapFn = snapFn,
                        onClick = { onHiveClick(hive.id) },
                        onDragEnd = { x, y -> viewModel.onHiveMoved(hive.id, x, y) },
                        onDeleteClick = { viewModel.onDeleteHive(hive.id) },
                    )
                }
            }

            if (hives.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (isEditMode)
                            "+ ile kovan ekle\nveya uzun bas"
                        else
                            "Kalem ikonuna bas\nsonra + veya uzun bas",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newHiveName = "" },
            title = { Text("Yeni Kovan") },
            text = {
                OutlinedTextField(
                    value = newHiveName,
                    onValueChange = { newHiveName = it },
                    label = { Text("Kovan adı") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newHiveName.isNotBlank()) {
                            viewModel.addHive(newHiveName.trim(), tapOffset.x, tapOffset.y)
                            showAddDialog = false
                            newHiveName = ""
                        }
                    }
                ) { Text("Ekle") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newHiveName = "" }) { Text("İptal") }
            },
        )
    }
}
