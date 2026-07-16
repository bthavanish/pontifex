package com.pontifex.app.domain.usecase

import com.pontifex.app.service.terminal.SessionManager
import com.pontifex.app.service.terminal.TerminalSession
import javax.inject.Inject

class StartShellSessionUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        containerPath: String,
        sessionId: Int,
        name: String? = null
    ): TerminalSession {
        return sessionManager.createSession(containerPath, sessionId, name)
    }
}
