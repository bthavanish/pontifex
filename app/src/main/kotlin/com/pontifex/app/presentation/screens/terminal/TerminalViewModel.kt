package com.pontifex.app.presentation.screens.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.domain.model.DeviceConnection
import com.pontifex.app.domain.repository.DeviceRepository
import com.pontifex.app.domain.repository.SettingsRepository
import com.pontifex.app.domain.usecase.ExecuteCommandUseCase
import com.pontifex.app.domain.usecase.StartShellSessionUseCase
import com.pontifex.app.service.terminal.SessionManager
import com.pontifex.app.service.terminal.TerminalSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val startShellSessionUseCase: StartShellSessionUseCase,
    private val executeCommandUseCase: ExecuteCommandUseCase,
    private val containerManager: ContainerManager,
    private val settingsRepository: SettingsRepository,
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<TerminalSession>>(emptyList())
    val sessions: StateFlow<List<TerminalSession>> = _sessions.asStateFlow()

    val activeSessionId: StateFlow<Int> = sessionManager.activeSessionId

    private val _scrollback = MutableStateFlow<List<com.pontifex.app.domain.model.TerminalLine>>(emptyList())
    val scrollback: StateFlow<List<com.pontifex.app.domain.model.TerminalLine>> = _scrollback.asStateFlow()

    val fontSize: StateFlow<Int> = settingsRepository.getFontSize()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)

    val showExtraKeys: StateFlow<Boolean> = settingsRepository.isShowExtraKeys()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val connectedDevices: StateFlow<List<DeviceConnection>> = deviceRepository.getConnectedDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var containerPath: String = ""

    init {
        viewModelScope.launch {
            settingsRepository.getContainerUri().collect { uri ->
                if (!uri.isNullOrBlank()) {
                    containerPath = containerManager.getContainerPath(uri)
                    if (_sessions.value.isEmpty()) {
                        createNewSession()
                    }
                }
            }
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            val sessionId = sessionManager.getNextSessionId()
            val session = startShellSessionUseCase(containerPath, sessionId)
            session.start()
            _sessions.value = _sessions.value + session

            viewModelScope.launch {
                session.scrollback.collect { lines ->
                    _scrollback.value = lines
                }
            }
        }
    }

    fun setActiveSession(sessionId: Int) {
        sessionManager.setActiveSession(sessionId)
        val session = sessionManager.getSession(sessionId)
        session?.let {
            viewModelScope.launch {
                it.scrollback.collect { lines ->
                    _scrollback.value = lines
                }
            }
        }
    }

    fun sendCommandToActiveSession(command: String) {
        viewModelScope.launch {
            sessionManager.getActiveSession()?.let { session ->
                executeCommandUseCase(session.id, command)
                sessionManager.saveCommandToHistory(session.id, command)
            }
        }
    }

    fun sendKeyToActiveSession(key: String) {
        viewModelScope.launch {
            sessionManager.getActiveSession()?.sendCommand(key)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionManager.closeAllSessions()
    }
}
