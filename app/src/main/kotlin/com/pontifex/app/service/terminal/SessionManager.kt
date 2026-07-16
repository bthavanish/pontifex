package com.pontifex.app.service.terminal

import android.content.Context
import com.pontifex.app.data.db.dao.CommandHistoryDao
import com.pontifex.app.data.db.entity.CommandHistoryEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commandHistoryDao: CommandHistoryDao
) {
    private val sessions = mutableMapOf<Int, TerminalSession>()
    private val _activeSessionId = MutableStateFlow(0)
    val activeSessionId: StateFlow<Int> = _activeSessionId.asStateFlow()

    private var nextSessionId = 1

    suspend fun createSession(
        containerPath: String,
        sessionId: Int,
        name: String? = null
    ): TerminalSession {
        val session = TerminalSession(
            id = sessionId,
            name = name ?: "Session $sessionId",
            containerPath = containerPath
        )
        sessions[sessionId] = session
        _activeSessionId.value = sessionId
        return session
    }

    fun getSession(sessionId: Int): TerminalSession? = sessions[sessionId]

    fun getActiveSession(): TerminalSession? = sessions[_activeSessionId.value]

    fun setActiveSession(sessionId: Int) {
        if (sessions.containsKey(sessionId)) {
            _activeSessionId.value = sessionId
        }
    }

    fun closeSession(sessionId: Int) {
        sessions[sessionId]?.destroy()
        sessions.remove(sessionId)
        if (_activeSessionId.value == sessionId) {
            _activeSessionId.value = sessions.keys.firstOrNull() ?: 0
        }
    }

    fun closeAllSessions() {
        sessions.values.forEach { it.destroy() }
        sessions.clear()
        _activeSessionId.value = 0
    }

    fun getNextSessionId(): Int = nextSessionId++

    suspend fun saveCommandToHistory(sessionId: Int, command: String) {
        commandHistoryDao.insert(
            CommandHistoryEntry(
                sessionId = sessionId,
                command = command
            )
        )
    }

    suspend fun getCommandHistory(sessionId: Int, limit: Int = 100): List<String> {
        return commandHistoryDao.getRecent(sessionId, limit).map { it.command }
    }
}
