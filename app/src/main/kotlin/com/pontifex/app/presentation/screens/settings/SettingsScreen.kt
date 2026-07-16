package com.pontifex.app.presentation.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val fontSize by viewModel.fontSize.collectAsState()
    val fontFamily by viewModel.fontFamily.collectAsState()
    val colorScheme by viewModel.colorScheme.collectAsState()
    val dynamicColor by viewModel.dynamicColor.collectAsState()
    val amoledBlack by viewModel.amoledBlack.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val showExtraKeys by viewModel.showExtraKeys.collectAsState()
    val cursorStyle by viewModel.cursorStyle.collectAsState()
    val cursorBlink by viewModel.cursorBlink.collectAsState()
    val scrollbackLines by viewModel.scrollbackLines.collectAsState()
    val defaultShell by viewModel.defaultShell.collectAsState()
    val bellSound by viewModel.bellSound.collectAsState()
    val vibrateOnBell by viewModel.vibrateOnBell.collectAsState()
    val defaultAdbPort by viewModel.defaultAdbPort.collectAsState()
    val wirelessScanTimeout by viewModel.wirelessScanTimeout.collectAsState()
    val keepAliveInterval by viewModel.keepAliveInterval.collectAsState()
    val autoReconnect by viewModel.autoReconnect.collectAsState()
    val binarySource by viewModel.binarySource.collectAsState()
    val adbVersion by viewModel.adbVersion.collectAsState()
    val fastbootVersion by viewModel.fastbootVersion.collectAsState()
    val adbSha256 by viewModel.adbSha256.collectAsState()
    val fastbootSha256 by viewModel.fastbootSha256.collectAsState()
    val containerRoot by viewModel.containerRoot.collectAsState()
    val containerSize by viewModel.containerSize.collectAsState()
    val workingDirectory by viewModel.workingDirectory.collectAsState()
    val appVersion by viewModel.appVersion.collectAsState()
    val buildHash by viewModel.buildHash.collectAsState()
    val autoCheckUpdates by viewModel.autoCheckUpdates.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showClearTransfersDialog by remember { mutableStateOf(false) }
    var showClearLogsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // === Appearance ===
            item { SettingsSectionHeader("Appearance") }

            item {
                SwitchSetting(
                    title = "Dynamic Color (Material You)",
                    description = "Use wallpaper-based color theming on Android 12+",
                    checked = dynamicColor,
                    onCheckedChange = { viewModel.updateDynamicColor(it) }
                )
            }

            item {
                RadioGroupSetting(
                    title = "Dark Mode",
                    options = listOf("System", "Light", "Dark"),
                    selectedIndex = when {
                        !darkMode -> 1
                        else -> 2
                    },
                    onSelected = { index -> viewModel.updateDarkMode(index != 1) }
                )
            }

            item {
                SwitchSetting(
                    title = "AMOLED Black",
                    description = "Use pure black background for OLED screens",
                    checked = amoledBlack,
                    onCheckedChange = { viewModel.updateAmoledBlack(it) }
                )
            }

            item {
                SliderSetting(
                    title = "Terminal Font Size",
                    value = fontSize.toFloat(),
                    valueRange = 8f..24f,
                    onValueChange = { viewModel.updateFontSize(it.toInt()) }
                )
            }

            item {
                DropdownSetting(
                    title = "Terminal Font",
                    value = fontFamily,
                    options = listOf("JetBrains Mono", "Fira Code", "Cascadia Code"),
                    onSelected = { viewModel.updateFontFamily(it) }
                )
            }

            item {
                DropdownSetting(
                    title = "Terminal Color Scheme",
                    value = colorScheme,
                    options = listOf("Default", "Dracula", "Solarized Dark", "Nord", "Gruvbox Dark", "Monokai"),
                    onSelected = { viewModel.updateColorScheme(it) }
                )
            }

            item {
                SwitchSetting(
                    title = "Show Extra Keys Row",
                    checked = showExtraKeys,
                    onCheckedChange = { viewModel.updateShowExtraKeys(it) }
                )
            }

            item {
                DropdownSetting(
                    title = "Cursor Style",
                    value = cursorStyle,
                    options = listOf("Block", "Underline", "Bar"),
                    onSelected = { viewModel.updateCursorStyle(it) }
                )
            }

            item {
                SwitchSetting(
                    title = "Cursor Blink",
                    checked = cursorBlink,
                    onCheckedChange = { viewModel.updateCursorBlink(it) }
                )
            }

            item {
                SliderSetting(
                    title = "Scrollback Lines",
                    value = scrollbackLines.toFloat(),
                    valueRange = 1000f..10000f,
                    onValueChange = { viewModel.updateScrollbackLines(it.toInt()) }
                )
            }

            // === ADB Binary ===
            item { SettingsSectionHeader("ADB Binary") }

            item {
                InfoRow(
                    label = "Current Path",
                    value = "$containerRoot/bin/adb"
                )
            }

            item {
                InfoRow(
                    label = "ADB Version",
                    value = adbVersion
                )
            }

            item {
                RadioGroupSetting(
                    title = "Binary Source",
                    options = listOf("Bundled", "Custom"),
                    selectedIndex = if (binarySource == "Bundled") 0 else 1,
                    onSelected = { index ->
                        viewModel.updateBinarySource(if (index == 0) "Bundled" else "Custom")
                    }
                )
            }

            if (binarySource == "Custom") {
                item {
                    ActionRow(
                        title = "Custom Binary Picker",
                        buttonLabel = "Pick File",
                        onClick = { viewModel.pickCustomAdb() }
                    )
                }
            }

            item {
                ChecksumRow(
                    label = "SHA-256",
                    value = adbSha256,
                    onCopy = { viewModel.copyToClipboard(adbSha256) }
                )
            }

            item {
                ActionRow(
                    title = "Verify Integrity",
                    buttonLabel = "Verify",
                    onClick = { viewModel.verifyBinaryIntegrity() }
                )
            }

            // === Fastboot Binary ===
            item { SettingsSectionHeader("Fastboot Binary") }

            item {
                InfoRow(
                    label = "Current Path",
                    value = "$containerRoot/bin/fastboot"
                )
            }

            item {
                InfoRow(
                    label = "Fastboot Version",
                    value = fastbootVersion
                )
            }

            item {
                RadioGroupSetting(
                    title = "Binary Source",
                    options = listOf("Bundled", "Custom"),
                    selectedIndex = if (binarySource == "Bundled") 0 else 1,
                    onSelected = { index ->
                        viewModel.updateBinarySource(if (index == 0) "Bundled" else "Custom")
                    }
                )
            }

            if (binarySource == "Custom") {
                item {
                    ActionRow(
                        title = "Custom Binary Picker",
                        buttonLabel = "Pick File",
                        onClick = { viewModel.pickCustomFastboot() }
                    )
                }
            }

            item {
                ChecksumRow(
                    label = "SHA-256",
                    value = fastbootSha256,
                    onCopy = { viewModel.copyToClipboard(fastbootSha256) }
                )
            }

            // === Virtual Container ===
            item { SettingsSectionHeader("Virtual Container") }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Container Root", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = containerRoot,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(onClick = { viewModel.changeContainerLocation() }) {
                        Text("Change")
                    }
                }
            }

            item {
                ActionRow(
                    title = "Reset Container",
                    buttonLabel = "Reset",
                    isDestructive = true,
                    onClick = { showResetDialog = true }
                )
            }

            item {
                InfoRow(
                    label = "Container Size",
                    value = containerSize
                )
            }

            // === Connection ===
            item { SettingsSectionHeader("Connection") }

            item {
                NumberSetting(
                    title = "Default ADB Port",
                    value = defaultAdbPort,
                    onValueChange = { viewModel.updateDefaultAdbPort(it) }
                )
            }

            item {
                SliderSetting(
                    title = "Wireless Scan Timeout",
                    value = wirelessScanTimeout.toFloat(),
                    valueRange = 3f..30f,
                    onValueChange = { viewModel.updateWirelessScanTimeout(it.toInt()) }
                )
            }

            item {
                DropdownSetting(
                    title = "Connection Keep-Alive Interval",
                    value = keepAliveInterval,
                    options = listOf("15s", "30s", "60s", "5m", "Never"),
                    onSelected = { viewModel.updateKeepAliveInterval(it) }
                )
            }

            item {
                SwitchSetting(
                    title = "Auto-Reconnect on Disconnect",
                    checked = autoReconnect,
                    onCheckedChange = { viewModel.updateAutoReconnect(it) }
                )
            }

            // === Shell ===
            item { SettingsSectionHeader("Shell") }

            item {
                DropdownSetting(
                    title = "Default Shell",
                    value = defaultShell,
                    options = listOf("sh", "bash"),
                    onSelected = { viewModel.updateDefaultShell(it) }
                )
            }

            item {
                SwitchSetting(
                    title = "Bell Sound",
                    checked = bellSound,
                    onCheckedChange = { viewModel.updateBellSound(it) }
                )
            }

            item {
                SwitchSetting(
                    title = "Vibrate on Bell",
                    checked = vibrateOnBell,
                    onCheckedChange = { viewModel.updateVibrateOnBell(it) }
                )
            }

            item {
                SwitchSetting(
                    title = "Allow External Storage Access",
                    description = "Grants container access to broader storage",
                    checked = false,
                    onCheckedChange = { }
                )
            }

            // === Storage ===
            item { SettingsSectionHeader("Storage") }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Working Directory", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = workingDirectory,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FilledTonalButton(onClick = { viewModel.changeWorkingDirectory() }) {
                        Text("Change")
                    }
                }
            }

            item {
                ActionRow(
                    title = "Clear Command History",
                    buttonLabel = "Clear",
                    isDestructive = true,
                    onClick = { showClearHistoryDialog = true }
                )
            }

            item {
                ActionRow(
                    title = "Clear Transfer History",
                    buttonLabel = "Clear",
                    isDestructive = true,
                    onClick = { showClearTransfersDialog = true }
                )
            }

            item {
                ActionRow(
                    title = "Clear Logs",
                    buttonLabel = "Clear",
                    isDestructive = true,
                    onClick = { showClearLogsDialog = true }
                )
            }

            // === Updates ===
            item { SettingsSectionHeader("Updates") }

            item {
                InfoRow(label = "Current Version", value = appVersion)
            }

            item {
                InfoRow(label = "Build Hash", value = buildHash)
            }

            item {
                ActionRow(
                    title = "Check for Updates",
                    buttonLabel = "Check",
                    onClick = { viewModel.checkForUpdates() }
                )
            }

            item {
                SwitchSetting(
                    title = "Auto-Check on Launch",
                    checked = autoCheckUpdates,
                    onCheckedChange = { viewModel.updateAutoCheckUpdates(it) }
                )
            }

            // === About ===
            item { SettingsSectionHeader("About") }

            item {
                InfoRow(label = "App Name", value = "Pontifex")
            }

            item {
                InfoRow(label = "Version", value = appVersion)
            }

            item {
                ActionRow(
                    title = "GitHub",
                    buttonLabel = "Open",
                    onClick = { viewModel.openGitHub() }
                )
            }

            item {
                ActionRow(
                    title = "Open Source Licenses",
                    buttonLabel = "View",
                    onClick = { viewModel.openLicenses() }
                )
            }

            item {
                ActionRow(
                    title = "Third-Party Notices",
                    buttonLabel = "View",
                    onClick = { viewModel.openNotices() }
                )
            }
        }
    }

    if (showResetDialog) {
        ConfirmDialog(
            title = "Reset Container",
            message = "This will delete all files in the container and re-initialize it. ADB and fastboot binaries will be re-extracted. Continue?",
            confirmLabel = "Reset",
            onConfirm = {
                viewModel.resetContainer()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }

    if (showClearHistoryDialog) {
        ConfirmDialog(
            title = "Clear Command History",
            message = "This will delete all terminal command history. Continue?",
            confirmLabel = "Clear",
            onConfirm = {
                viewModel.clearCommandHistory()
                showClearHistoryDialog = false
            },
            onDismiss = { showClearHistoryDialog = false }
        )
    }

    if (showClearTransfersDialog) {
        ConfirmDialog(
            title = "Clear Transfer History",
            message = "This will delete all file transfer history. Continue?",
            confirmLabel = "Clear",
            onConfirm = {
                viewModel.clearTransferHistory()
                showClearTransfersDialog = false
            },
            onDismiss = { showClearTransfersDialog = false }
        )
    }

    if (showClearLogsDialog) {
        ConfirmDialog(
            title = "Clear Logs",
            message = "This will delete all log files. Continue?",
            confirmLabel = "Clear",
            onConfirm = {
                viewModel.clearLogs()
                showClearLogsDialog = false
            },
            onDismiss = { showClearLogsDialog = false }
        )
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun SwitchSetting(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SliderSetting(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Text(text = "$title: ${value.toInt()}", style = MaterialTheme.typography.bodyLarge)
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange)
    }
}

@Composable
private fun DropdownSetting(
    title: String,
    value: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        options.forEach { option ->
            Text(
                text = option,
                style = MaterialTheme.typography.bodyMedium,
                color = if (option == value) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(option) }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun RadioGroupSetting(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(4.dp))
        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelected(index) }
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = index == selectedIndex,
                    onClick = { onSelected(index) }
                )
                Text(
                    text = option,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionRow(
    title: String,
    buttonLabel: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (isDestructive) {
            OutlinedButton(onClick = onClick) {
                Text(buttonLabel, color = MaterialTheme.colorScheme.error)
            }
        } else {
            FilledTonalButton(onClick = onClick) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun ChecksumRow(label: String, value: String, onCopy: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onCopy) {
            Icon(
                Icons.Default.ContentCopy,
                contentDescription = "Copy",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NumberSetting(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(value.toString()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        androidx.compose.material3.OutlinedTextField(
            value = textValue,
            onValueChange = { newText ->
                textValue = newText
                newText.toIntOrNull()?.let { onValueChange(it) }
            },
            modifier = Modifier.width(80.dp),
            singleLine = true
        )
    }
}
