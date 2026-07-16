package com.pontifex.app.presentation.screens.files

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.domain.repository.SettingsRepository
import com.pontifex.app.domain.usecase.ExecuteCommandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class FileBrowserViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val containerManager: ContainerManager,
    private val binaryManager: BinaryManager,
    private val executeCommandUseCase: ExecuteCommandUseCase
) : ViewModel() {

    private val _hostFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val hostFiles: StateFlow<List<FileItem>> = _hostFiles.asStateFlow()

    private val _targetFiles = MutableStateFlow<List<FileItem>>(emptyList())
    val targetFiles: StateFlow<List<FileItem>> = _targetFiles.asStateFlow()

    private val _hostBreadcrumb = MutableStateFlow<List<String>>(listOf("work"))
    val hostBreadcrumb: StateFlow<List<String>> = _hostBreadcrumb.asStateFlow()

    private val _targetBreadcrumb = MutableStateFlow<List<String>>(listOf("sdcard"))
    val targetBreadcrumb: StateFlow<List<String>> = _targetBreadcrumb.asStateFlow()

    private val _selectedHostFiles = MutableStateFlow<Set<String>>(emptySet())
    val selectedHostFiles: StateFlow<Set<String>> = _selectedHostFiles.asStateFlow()

    private val _selectedTargetFiles = MutableStateFlow<Set<String>>(emptySet())
    val selectedTargetFiles: StateFlow<Set<String>> = _selectedTargetFiles.asStateFlow()

    private val _isWideScreen = MutableStateFlow(false)
    val isWideScreen: StateFlow<Boolean> = _isWideScreen.asStateFlow()

    private val _isTransferring = MutableStateFlow(false)
    val isTransferring: StateFlow<Boolean> = _isTransferring.asStateFlow()

    private var containerPath: String = ""

    init {
        viewModelScope.launch {
            settingsRepository.getContainerUri().collect { uri ->
                if (!uri.isNullOrBlank()) {
                    containerPath = containerManager.getContainerPath(uri)
                    loadHostFiles()
                }
            }
        }
    }

    fun setWideScreen(wide: Boolean) {
        _isWideScreen.value = wide
    }

    private fun loadHostFiles() {
        viewModelScope.launch {
            val path = "$containerPath/${_hostBreadcrumb.value.joinToString("/")}"
            val dir = java.io.File(path)
            if (dir.exists() && dir.isDirectory) {
                _hostFiles.value = dir.listFiles()?.map { file ->
                    FileItem(
                        name = file.name,
                        path = file.absolutePath,
                        isDirectory = file.isDirectory,
                        size = if (file.isDirectory) "-" else formatSize(file.length())
                    )
                }?.sortedWith(compareByDescending<FileItem> { it.isDirectory }.thenBy { it.name })
                    ?: emptyList()
            }
        }
    }

    private fun loadTargetFiles() {
        viewModelScope.launch {
            val path = "/${_targetBreadcrumb.value.joinToString("/")}"
            val output = runAdbCommand("shell ls -la $path")
            _targetFiles.value = parseLsOutput(output).filter { it.name != "." && it.name != ".." }
        }
    }

    private fun parseLsOutput(output: String): List<FileItem> {
        return output.lines().filter { it.isNotBlank() }.mapNotNull { line ->
            val parts = line.split("\\s+".toRegex())
            if (parts.size >= 8) {
                val isDir = parts[0].startsWith("d")
                val name = parts.drop(7).joinToString(" ")
                val size = if (isDir) "-" else parts[4]
                FileItem(
                    name = name,
                    path = name,
                    isDirectory = isDir,
                    size = size
                )
            } else null
        }
    }

    fun onHostFileClick(file: FileItem) {
        if (file.isDirectory) {
            _hostBreadcrumb.value = _hostBreadcrumb.value + file.name
            loadHostFiles()
        }
    }

    fun onTargetFileClick(file: FileItem) {
        if (file.isDirectory) {
            _targetBreadcrumb.value = _targetBreadcrumb.value + file.name
            loadTargetFiles()
        }
    }

    fun onHostBreadcrumbClick(index: Int) {
        _hostBreadcrumb.value = _hostBreadcrumb.value.take(index + 1)
        loadHostFiles()
    }

    fun onTargetBreadcrumbClick(index: Int) {
        _targetBreadcrumb.value = _targetBreadcrumb.value.take(index + 1)
        loadTargetFiles()
    }

    fun toggleHostFileSelection(file: FileItem) {
        _selectedHostFiles.value = if (file.path in _selectedHostFiles.value) {
            _selectedHostFiles.value - file.path
        } else {
            _selectedHostFiles.value + file.path
        }
    }

    fun toggleTargetFileSelection(file: FileItem) {
        _selectedTargetFiles.value = if (file.path in _selectedTargetFiles.value) {
            _selectedTargetFiles.value - file.path
        } else {
            _selectedTargetFiles.value + file.path
        }
    }

    fun refresh() {
        loadHostFiles()
        loadTargetFiles()
    }

    fun pushFiles() {
        viewModelScope.launch {
            _isTransferring.value = true
            val targetPath = "/${_targetBreadcrumb.value.joinToString("/")}"
            _selectedHostFiles.value.forEach { file ->
                val hostPath = "$containerPath/${_hostBreadcrumb.value.joinToString("/")}"
                val cmd = "adb push $hostPath/${java.io.File(file).name} $targetPath/"
                executeCommandUseCase(0, cmd)
                    .onEach { }
                    .launchIn(viewModelScope)
            }
            _selectedHostFiles.value = emptySet()
            loadTargetFiles()
            _isTransferring.value = false
        }
    }

    fun pullFiles() {
        viewModelScope.launch {
            _isTransferring.value = true
            val hostPath = "$containerPath/${_hostBreadcrumb.value.joinToString("/")}"
            val targetPath = "/${_targetBreadcrumb.value.joinToString("/")}"
            _selectedTargetFiles.value.forEach { file ->
                val cmd = "adb pull $targetPath/$file $hostPath/"
                executeCommandUseCase(0, cmd)
                    .onEach { }
                    .launchIn(viewModelScope)
            }
            _selectedTargetFiles.value = emptySet()
            loadHostFiles()
            _isTransferring.value = false
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _selectedHostFiles.value.forEach { file ->
                java.io.File(file).delete()
            }
            _selectedHostFiles.value = emptySet()
            loadHostFiles()

            val targetPath = "/${_targetBreadcrumb.value.joinToString("/")}"
            _selectedTargetFiles.value.forEach { file ->
                runAdbCommand("shell rm -rf $targetPath/$file")
            }
            _selectedTargetFiles.value = emptySet()
            loadTargetFiles()
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val path = "$containerPath/${_hostBreadcrumb.value.joinToString("/")}/$name"
            java.io.File(path).mkdirs()
            loadHostFiles()
        }
    }

    private suspend fun runAdbCommand(args: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val adbPath = if (containerPath.isNotBlank()) {
                    binaryManager.getAdbPath(containerPath)
                } else {
                    "adb"
                }
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "$adbPath $args"))
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val error = BufferedReader(InputStreamReader(process.errorStream))
                val output = reader.readText()
                val errorOutput = error.readText()
                process.waitFor()
                output.ifBlank { errorOutput }
            } catch (e: Exception) {
                "Error: ${e.message}"
            }
        }
    }

    private fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            else -> "${bytes / (1024 * 1024 * 1024)} GB"
        }
    }
}
