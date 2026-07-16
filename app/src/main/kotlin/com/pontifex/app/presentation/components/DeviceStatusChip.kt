package com.pontifex.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pontifex.app.domain.model.DeviceConnection
import com.pontifex.app.domain.model.DeviceState

@Composable
fun DeviceStatusChip(
    devices: List<DeviceConnection>,
    modifier: Modifier = Modifier
) {
    when {
        devices.isEmpty() -> {
            AssistChip(
                onClick = { },
                label = { Text("No device") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = modifier
            )
        }
        devices.size == 1 -> {
            val device = devices.first()
            AssistChip(
                onClick = { },
                label = {
                    Text("${device.name} (${device.connectionType::class.simpleName})")
                },
                leadingIcon = {
                    when (device.state) {
                        DeviceState.Online -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        DeviceState.Bootloader -> {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        else -> {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = when (device.state) {
                        DeviceState.Online -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                ),
                modifier = modifier
            )
        }
        else -> {
            AssistChip(
                onClick = { },
                label = { Text("${devices.size} devices") },
                leadingIcon = {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                },
                modifier = modifier
            )
        }
    }
}
