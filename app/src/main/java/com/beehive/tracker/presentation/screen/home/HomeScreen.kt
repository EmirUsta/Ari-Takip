package com.beehive.tracker.presentation.screen.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onApiaryClick: (apiaryId: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val apiaries by viewModel.apiaries.collectAsStateWithLifecycle()
    val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var exportApiaryId by remember { mutableStateOf<String?>(null) }
    var deleteCandidate by remember { mutableStateOf<com.beehive.tracker.domain.model.Apiary?>(null) }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        val id = exportApiaryId ?: return@rememberLauncherForActivityResult
        uri?.let {
            val os = context.contentResolver.openOutputStream(it) ?: return@let
            viewModel.export(id, os)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val stream = context.contentResolver.openInputStream(it) ?: return@let
            viewModel.import(stream)
        }
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Arılıklarım") },
                actions = {
                    IconButton(onClick = { importLauncher.launch(arrayOf("application/zip", "application/json", "*/*")) }) {
                        Icon(Icons.Default.FileUpload, contentDescription = "Arılık İçe Aktar")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Yeni arılık")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (apiaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Henüz arılık yok.\n+ butonuna basarak ekle.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                items(apiaries, key = { it.id }) { apiary ->
                    var showMenu by remember { mutableStateOf(false) }
                    ListItem(
                        headlineContent = { Text(apiary.name) },
                        supportingContent = {
                            if (apiary.locationNote.isNotBlank()) Text(apiary.locationNote)
                        },
                        trailingContent = {
                            Box {
                                IconButton(onClick = { showMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "Seçenekler")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Dışa Aktar") },
                                        onClick = {
                                            showMenu = false
                                            exportApiaryId = apiary.id
                                            exportLauncher.launch("kovantakip-${apiary.name}.zip")
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                                        onClick = {
                                            showMenu = false
                                            deleteCandidate = apiary
                                        },
                                    )
                                }
                            }
                        },
                        modifier = Modifier.clickable { onApiaryClick(apiary.id) },
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    // Yeni arılık dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newName = "" },
            title = { Text("Yeni Arılık") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Arılık adı") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.addApiary(newName.trim())
                            showAddDialog = false
                            newName = ""
                        }
                    }
                ) { Text("Ekle") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; newName = "" }) { Text("İptal") }
            },
        )
    }

    // Silme onay dialog
    deleteCandidate?.let { apiary ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("Arılığı Sil") },
            text = { Text("\"${apiary.name}\" arılığı ve tüm kovan/notları kalıcı olarak silinecek. Devam edilsin mi?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteApiary(apiary.id)
                        deleteCandidate = null
                    },
                ) { Text("Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) { Text("İptal") }
            },
        )
    }
}
