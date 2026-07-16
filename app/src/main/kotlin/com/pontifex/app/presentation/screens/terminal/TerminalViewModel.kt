package com.pontifex.app.presentation.screens.terminal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.domain.model.DeviceConnection
import com.pontifex.app.domain.repository.DeviceRepository
import com.pontifex.app.domain.repository.SettingsRepository
import com.pontifex.app.domain.usecase.ExecuteCommandUseCase
import com.pontifex.app.domain.usecase.StartShellSessionUseCase
import com.pontifex.app.service.device.DeviceMonitor
import com.pontifex.app.service.terminal.PontifexTerminalSession
import com.pontifex.app.service.terminal.SessionManager
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
    private val deviceRepository: DeviceRepository,
    private val deviceMonitor: DeviceMonitor
) : ViewModel() {

    private val _sessions = MutableStateFlow<List<PontifexTerminalSession>>(emptyList())
    val sessions: StateFlow<List<PontifexTerminalSession>> = _sessions.asStateFlow()

    private val _activeSession = MutableStateFlow<PontifexTerminalSession?>(null)
    val activeSession: StateFlow<PontifexTerminalSession?> = _activeSession.asStateFlow()

    val activeSessionId: StateFlow<Int> = sessionManager.activeSessionId

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

        viewModelScope.launch {
            deviceMonitor.devices.collect { devices ->
                deviceRepository.syncDevices(devices)
            }
        }
    }

    fun createNewSession() {
        val sessionId = sessionManager.getNextSessionId()
        val session = startShellSessionUseCase(containerPath, sessionId)
        session.start()
        _sessions.value = _sessions.value + session
        _activeSession.value = session
        sessionManager.setActiveSession(sessionId)
    }

    fun setActiveSession(sessionId: Int) {
        sessionManager.setActiveSession(sessionId)
        val session = sessionManager.getSession(sessionId)
        _activeSession.value = session
    }

    fun closeSession(sessionId: Int) {
        sessionManager.closeSession(sessionId)
        _sessions.value = _sessions.value.filter { it.id != sessionId }
        _activeSession.value = sessionManager.getActiveSession()
    }

    fun sendCommandToActiveSession(command: String) {
        viewModelScope.launch {
            sessionManager.getActiveSession()?.let { session ->
                session.write(command + "\r")
                sessionManager.saveCommandToHistory(session.id, command)
            }
        }
    }

    fun sendKeyToActiveSession(key: String) {
        viewModelScope.launch {
            sessionManager.getActiveSession()?.write(key)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionManager.closeAllSessions()
        deviceMonitor.stop()
    }
}
