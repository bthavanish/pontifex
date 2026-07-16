package com.pontifex.app.domain.usecase

import com.pontifex.app.service.terminal.PontifexTerminalSession
import com.pontifex.app.service.terminal.SessionManager
import javax.inject.Inject

class StartShellSessionUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    operator fun invoke(
        containerPath: String,
        sessionId: Int,
        name: String? = null
    ): PontifexTerminalSession {
        return sessionManager.createSession(containerPath, sessionId, name)
    }
}
