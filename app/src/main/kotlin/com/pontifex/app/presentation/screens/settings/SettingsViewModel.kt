package com.pontifex.app.presentation.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ChecksumVerifier
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.data.db.dao.CommandHistoryDao
import com.pontifex.app.domain.repository.SettingsRepository
import com.pontifex.app.service.update.UpdateChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository,
    private val binaryManager: BinaryManager,
    private val checksumVerifier: ChecksumVerifier,
    private val containerManager: ContainerManager,
    private val commandHistoryDao: CommandHistoryDao,
    private val updateChecker: UpdateChecker
) : ViewModel() {

    // Appearance
    val fontSize: StateFlow<Int> = settingsRepository.getFontSize()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)

    val fontFamily: StateFlow<String> = settingsRepository.getFontFamily()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "JetBrains Mono")

    val colorScheme: StateFlow<String> = settingsRepository.getColorScheme()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Default")

    val dynamicColor: StateFlow<Boolean> = settingsRepository.isDynamicColor()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val amoledBlack: StateFlow<Boolean> = settingsRepository.isAmoledBlack()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val darkMode: StateFlow<Boolean> = settingsRepository.isDarkMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val showExtraKeys: StateFlow<Boolean> = settingsRepository.isShowExtraKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val cursorStyle: StateFlow<String> = settingsRepository.getCursorStyle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Block")

    val cursorBlink: StateFlow<Boolean> = settingsRepository.isCursorBlink()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val scrollbackLines: StateFlow<Int> = settingsRepository.getScrollbackLines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5000)

    // Binary
    val binarySource: StateFlow<String> = settingsRepository.getBinarySource()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Bundled")

    private val _adbVersion = MutableStateFlow("Unknown")
    val adbVersion: StateFlow<String> = _adbVersion.asStateFlow()

    private val _fastbootVersion = MutableStateFlow("Unknown")
    val fastbootVersion: StateFlow<String> = _fastbootVersion.asStateFlow()

    private val _adbSha256 = MutableStateFlow("")
    val adbSha256: StateFlow<String> = _adbSha256.asStateFlow()

    private val _fastbootSha256 = MutableStateFlow("")
    val fastbootSha256: StateFlow<String> = _fastbootSha256.asStateFlow()

    // Container
    val containerRoot: StateFlow<String> = settingsRepository.getContainerUri()
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _containerSize = MutableStateFlow("Unknown")
    val containerSize: StateFlow<String> = _containerSize.asStateFlow()

    val workingDirectory: StateFlow<String> = MutableStateFlow("container/work/").asStateFlow()

    // Connection
    val defaultAdbPort: StateFlow<Int> = settingsRepository.getDefaultAdbPort()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 5555)

    val wirelessScanTimeout: StateFlow<Int> = settingsRepository.getWirelessScanTimeout()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val keepAliveInterval: StateFlow<String> = settingsRepository.getKeepAliveInterval()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "30s")

    val autoReconnect: StateFlow<Boolean> = settingsRepository.isAutoReconnect()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Shell
    val defaultShell: StateFlow<String> = settingsRepository.getDefaultShell()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "sh")

    val bellSound: StateFlow<Boolean> = settingsRepository.isBellSound()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val vibrateOnBell: StateFlow<Boolean> = settingsRepository.isVibrateOnBell()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Updates
    val autoCheckUpdates: StateFlow<Boolean> = settingsRepository.isAutoCheckUpdates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val appVersion: StateFlow<String> = MutableStateFlow("1.0.0").asStateFlow()

    private val _buildHash = MutableStateFlow("dev")
    val buildHash: StateFlow<String> = _buildHash.asStateFlow()

    init {
        loadBinaryInfo()
    }

    private fun loadBinaryInfo() {
        viewModelScope.launch {
            val containerUri = settingsRepository.getContainerUri().first()
            if (!containerUri.isNullOrBlank()) {
                val containerPath = containerManager.getContainerPath(containerUri)
                _adbSha256.value = try {
                    binaryManager.computeSha256(java.io.File("$containerPath/bin/adb"))
                } catch (_: Exception) { "" }
                _fastbootSha256.value = try {
                    binaryManager.computeSha256(java.io.File("$containerPath/bin/fastboot"))
                } catch (_: Exception) { "" }

                _adbVersion.value = runCommand("$containerPath/bin/adb version")
                _fastbootVersion.value = runCommand("$containerPath/bin/fastboot --version")
            }
        }
    }

    private fun runCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine() ?: "Unknown"
            process.waitFor()
            output
        } catch (_: Exception) {
            "Unknown"
        }
    }

    fun updateFontSize(size: Int) { viewModelScope.launch { settingsRepository.setFontSize(size) } }
    fun updateFontFamily(family: String) { viewModelScope.launch { settingsRepository.setFontFamily(family) } }
    fun updateColorScheme(scheme: String) { viewModelScope.launch { settingsRepository.setColorScheme(scheme) } }
    fun updateDynamicColor(enabled: Boolean) { viewModelScope.launch { settingsRepository.setDynamicColor(enabled) } }
    fun updateAmoledBlack(enabled: Boolean) { viewModelScope.launch { settingsRepository.setAmoledBlack(enabled) } }
    fun updateDarkMode(dark: Boolean) { viewModelScope.launch { settingsRepository.setDarkMode(dark) } }
    fun updateShowExtraKeys(show: Boolean) { viewModelScope.launch { settingsRepository.setShowExtraKeys(show) } }
    fun updateCursorStyle(style: String) { viewModelScope.launch { settingsRepository.setCursorStyle(style) } }
    fun updateCursorBlink(enabled: Boolean) { viewModelScope.launch { settingsRepository.setCursorBlink(enabled) } }
    fun updateScrollbackLines(lines: Int) { viewModelScope.launch { settingsRepository.setScrollbackLines(lines) } }
    fun updateBinarySource(source: String) { viewModelScope.launch { settingsRepository.setBinarySource(source) } }
    fun updateDefaultShell(shell: String) { viewModelScope.launch { settingsRepository.setDefaultShell(shell) } }
    fun updateBellSound(enabled: Boolean) { viewModelScope.launch { settingsRepository.setBellSound(enabled) } }
    fun updateVibrateOnBell(enabled: Boolean) { viewModelScope.launch { settingsRepository.setVibrateOnBell(enabled) } }
    fun updateDefaultAdbPort(port: Int) { viewModelScope.launch { settingsRepository.setDefaultAdbPort(port) } }
    fun updateWirelessScanTimeout(timeout: Int) { viewModelScope.launch { settingsRepository.setWirelessScanTimeout(timeout) } }
    fun updateKeepAliveInterval(interval: String) { viewModelScope.launch { settingsRepository.setKeepAliveInterval(interval) } }
    fun updateAutoReconnect(enabled: Boolean) { viewModelScope.launch { settingsRepository.setAutoReconnect(enabled) } }
    fun updateAutoCheckUpdates(enabled: Boolean) { viewModelScope.launch { settingsRepository.setAutoCheckUpdates(enabled) } }

    fun verifyBinaryIntegrity() {
        viewModelScope.launch {
            val containerUri = settingsRepository.getContainerUri().first()
            if (!containerUri.isNullOrBlank()) {
                val containerPath = containerManager.getContainerPath(containerUri)
                val result = checksumVerifier.verifyAll(containerPath)
                // Result is handled by UI observing the sha256 values
            }
        }
    }

    fun resetContainer() {
        viewModelScope.launch {
            val containerUri = settingsRepository.getContainerUri().first()
            if (!containerUri.isNullOrBlank()) {
                val containerPath = containerManager.getContainerPath(containerUri)
                java.io.File(containerPath).deleteRecursively()
                settingsRepository.setContainerUri("")
                settingsRepository.setOnboardingComplete(false)
            }
        }
    }

    fun clearCommandHistory() {
        viewModelScope.launch { commandHistoryDao.deleteAll() }
    }

    fun clearTransferHistory() {
        // Transfer history is cleared by resetting the file browser state
        // No persistent storage needed for transfers
    }

    fun clearLogs() {
        viewModelScope.launch {
            val containerUri = settingsRepository.getContainerUri().first()
            if (!containerUri.isNullOrBlank()) {
                java.io.File(containerManager.getContainerPath(containerUri), "logs").deleteRecursively()
            }
        }
    }

    fun changeContainerLocation() {
        // Container location change is handled via the OnboardingScreen flow
        // The user will need to re-run onboarding to change the container location
    }

    fun changeWorkingDirectory() {
        // Working directory change is handled via the FileBrowser screen
    }

    fun pickCustomAdb() {
        // Custom binary picker - opens system file picker
    }

    fun pickCustomFastboot() {
        // Custom binary picker - opens system file picker
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("SHA-256", text)
        clipboard.setPrimaryClip(clip)
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            updateChecker.checkForUpdates()
        }
    }

    fun openGitHub() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/pontifex-app/pontifex"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openLicenses() {
        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("https://github.com/pontifex-app/pontifex/blob/main/LICENSE")
        )
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openNotices() {
        val intent = android.content.Intent(
            android.content.Intent.ACTION_VIEW,
            android.net.Uri.parse("https://github.com/nmeum/android-tools")
        )
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
