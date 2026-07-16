package com.pontifex.app.presentation.screens.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.pontifex.app.presentation.components.DeviceStatusChip
import com.pontifex.app.presentation.components.ExtraKeysRow
import com.pontifex.app.presentation.components.TerminalInputRow
import com.pontifex.app.presentation.components.TerminalView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val sessions by viewModel.sessions.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val showExtraKeys by viewModel.showExtraKeys.collectAsState()
    val connectedDevices by viewModel.connectedDevices.collectAsState()

    var showSessionSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = activeSession?.title?.collectAsState()?.value ?: "Terminal",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    DeviceStatusChip(devices = connectedDevices)
                    IconButton(onClick = { showSessionSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Sessions"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = activeSession == null,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ),
                exit = shrinkVertically()
            ) {
                FloatingActionButton(
                    onClick = { viewModel.createNewSession() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(Icons.Default.Add, contentDescription = "New Session")
                }
            }
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val screen by activeSession?.screen?.collectAsState() ?: remember { androidx.compose.runtime.mutableStateOf(null) }
            val cursorRow by activeSession?.cursorRow?.collectAsState() ?: remember { androidx.compose.runtime.mutableIntStateOf(0) }
            val cursorCol by activeSession?.cursorCol?.collectAsState() ?: remember { androidx.compose.runtime.mutableIntStateOf(0) }
            val columns by activeSession?.columns?.collectAsState() ?: remember { androidx.compose.runtime.mutableIntStateOf(80) }
            val colors by activeSession?.colors?.collectAsState() ?: remember { mutableStateOf(null) }

            TerminalView(
                screen = screen,
                columns = columns,
                cursorRow = cursorRow,
                cursorCol = cursorCol,
                colors = colors,
                fontSize = fontSize,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            AnimatedVisibility(
                visible = showExtraKeys,
                enter = expandVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                ),
                exit = shrinkVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                )
            ) {
                ExtraKeysRow(
                    session = activeSession,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            TerminalInputRow(
                session = activeSession,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showSessionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSessionSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                sessions.forEach { session ->
                    TextButton(
                        onClick = {
                            viewModel.setActiveSession(session.id)
                            scope.launch { sheetState.hide() }
                            showSessionSheet = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = session.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = {
                            viewModel.closeSession(session.id)
                        }) {
                            Text("Close")
                        }
                    }
                }

                TextButton(
                    onClick = {
                        viewModel.createNewSession()
                        scope.launch { sheetState.hide() }
                        showSessionSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("New Session")
                }
            }
        }
    }
}
