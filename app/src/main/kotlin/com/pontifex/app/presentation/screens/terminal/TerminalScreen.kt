package com.pontifex.app.presentation.screens.terminal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.pontifex.app.presentation.components.DeviceStatusChip
import com.pontifex.app.presentation.components.ExtraKeysRow
import com.pontifex.app.presentation.components.TerminalInputRow
import com.pontifex.app.presentation.components.TerminalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val scrollback by viewModel.scrollback.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val showExtraKeys by viewModel.showExtraKeys.collectAsState()
    val connectedDevices by viewModel.connectedDevices.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terminal") },
                actions = {
                    DeviceStatusChip(devices = connectedDevices)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (sessions.isNotEmpty()) {
                TabRow(selectedTabIndex = selectedTabIndex.coerceIn(0, sessions.size - 1)) {
                    sessions.forEachIndexed { index, session ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                selectedTabIndex = index
                                viewModel.setActiveSession(session.id)
                            },
                            text = { Text(session.name) }
                        )
                    }
                    Tab(
                        selected = false,
                        onClick = { viewModel.createNewSession() },
                        text = { Text("+") }
                    )
                }
            }

            TerminalView(
                lines = scrollback,
                fontSize = fontSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            if (showExtraKeys) {
                ExtraKeysRow(
                    onKeyPress = { key -> viewModel.sendKeyToActiveSession(key) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            TerminalInputRow(
                onSendCommand = { command -> viewModel.sendCommandToActiveSession(command) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
