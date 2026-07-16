package com.pontifex.app.domain.usecase

import com.pontifex.app.service.terminal.SessionManager
import com.pontifex.app.service.terminal.TerminalSession
import javax.inject.Inject

class ExecuteCommandUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        sessionId: Int,
        command: String
    ): Result<Unit> {
        val session = sessionManager.getSession(sessionId)
            ?: return Result.failure(IllegalStateException("Session $sessionId not found"))
        session.sendCommand(command)
        return Result.success(Unit)
    }
}
