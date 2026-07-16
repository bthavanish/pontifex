package com.pontifex.app.presentation.screens.files

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel = hiltViewModel()
) {
    val hostFiles by viewModel.hostFiles.collectAsState()
    val targetFiles by viewModel.targetFiles.collectAsState()
    val hostBreadcrumb by viewModel.hostBreadcrumb.collectAsState()
    val targetBreadcrumb by viewModel.targetBreadcrumb.collectAsState()
    val isWideScreen by viewModel.isWideScreen.collectAsState()
    val selectedHostFiles by viewModel.selectedHostFiles.collectAsState()
    val selectedTargetFiles by viewModel.selectedTargetFiles.collectAsState()

    var showNewFolderDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Files") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isWideScreen) {
                Row(modifier = Modifier.weight(1f)) {
                    FilePane(
                        title = "Host (Container)",
                        files = hostFiles,
                        breadcrumb = hostBreadcrumb,
                        selectedFiles = selectedHostFiles,
                        onFileClick = { viewModel.onHostFileClick(it) },
                        onBreadcrumbClick = { viewModel.onHostBreadcrumbClick(it) },
                        onFileSelect = { viewModel.toggleHostFileSelection(it) },
                        modifier = Modifier.weight(1f)
                    )
                    VerticalDivider()
                    FilePane(
                        title = "Target Device",
                        files = targetFiles,
                        breadcrumb = targetBreadcrumb,
                        selectedFiles = selectedTargetFiles,
                        onFileClick = { viewModel.onTargetFileClick(it) },
                        onBreadcrumbClick = { viewModel.onTargetBreadcrumbClick(it) },
                        onFileSelect = { viewModel.toggleTargetFileSelection(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                var selectedTab by remember { mutableStateOf(0) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Host") }
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Target") }
                    )
                }

                if (selectedTab == 0) {
                    FilePane(
                        title = "Host (Container)",
                        files = hostFiles,
                        breadcrumb = hostBreadcrumb,
                        selectedFiles = selectedHostFiles,
                        onFileClick = { viewModel.onHostFileClick(it) },
                        onBreadcrumbClick = { viewModel.onHostBreadcrumbClick(it) },
                        onFileSelect = { viewModel.toggleHostFileSelection(it) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    FilePane(
                        title = "Target Device",
                        files = targetFiles,
                        breadcrumb = targetBreadcrumb,
                        selectedFiles = selectedTargetFiles,
                        onFileClick = { viewModel.onTargetFileClick(it) },
                        onBreadcrumbClick = { viewModel.onTargetBreadcrumbClick(it) },
                        onFileSelect = { viewModel.toggleTargetFileSelection(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { viewModel.pushFiles() },
                    enabled = selectedHostFiles.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Push \u2192")
                }
                FilledTonalButton(
                    onClick = { viewModel.pullFiles() },
                    enabled = selectedTargetFiles.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("\u2190 Pull")
                }
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    enabled = selectedHostFiles.isNotEmpty() || selectedTargetFiles.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("Delete")
                }
                OutlinedButton(
                    onClick = { showNewFolderDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                    Text("New")
                }
            }
        }
    }

    if (showNewFolderDialog) {
        var folderName by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showNewFolderDialog = false },
            title = { Text("New Folder") },
            text = {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder name") },
                    singleLine = true
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        if (folderName.isNotBlank()) {
                            viewModel.createFolder(folderName)
                            showNewFolderDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showNewFolderDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        val count = selectedHostFiles.size + selectedTargetFiles.size
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Files") },
            text = { Text("Delete $count selected file(s)?") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        viewModel.deleteSelected()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun FilePane(
    title: String,
    files: List<FileItem>,
    breadcrumb: List<String>,
    selectedFiles: Set<String>,
    onFileClick: (FileItem) -> Unit,
    onBreadcrumbClick: (Int) -> Unit,
    onFileSelect: (FileItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            breadcrumb.forEachIndexed { index, segment ->
                Text(
                    text = if (index == 0) "/" else "$segment > ",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (index == breadcrumb.lastIndex) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable { onBreadcrumbClick(index) }
                )
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(files) { file ->
                FileRow(
                    file = file,
                    isSelected = file.path in selectedFiles,
                    onClick = {
                        if (file.isDirectory) onFileClick(file) else onFileSelect(file)
                    },
                    onLongClick = { onFileSelect(file) }
                )
            }
        }
    }
}

@Composable
private fun FileRow(
    file: FileItem,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = if (file.isDirectory) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 12.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = file.size,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isSelected) {
            Icon(
                Icons.Default.FolderOpen,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun VerticalDivider() {
    HorizontalDivider(
        modifier = Modifier
            .width(1.dp)
            .padding(vertical = 8.dp)
    )
}

data class FileItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: String
)
