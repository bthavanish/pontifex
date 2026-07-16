package com.pontifex.app.domain.usecase

import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.data.db.dao.CommandHistoryDao
import com.pontifex.app.data.db.entity.CommandHistoryEntry
import com.pontifex.app.domain.repository.DeviceRepository
import com.pontifex.app.domain.repository.SettingsRepository
import com.pontifex.app.service.terminal.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

enum class CommandType { Adb, Fastboot, Shell, Auto }

sealed class CommandOutput {
    data class Line(val text: String, val isError: Boolean) : CommandOutput()
    data class Exit(val code: Int) : CommandOutput()
}

class ExecuteCommandUseCase @Inject constructor(
    private val sessionManager: SessionManager,
    private val deviceRepository: DeviceRepository,
    private val binaryManager: BinaryManager,
    private val containerManager: ContainerManager,
    private val settingsRepository: SettingsRepository,
    private val commandHistoryDao: CommandHistoryDao
) {
    operator fun invoke(
        sessionId: Int,
        command: String,
        serial: String? = null,
        commandType: CommandType = CommandType.Auto
    ): Flow<CommandOutput> = flow {
        val containerUri = settingsRepository.getContainerUri().first()
        val containerPath = if (!containerUri.isNullOrBlank()) {
            containerManager.getContainerPath(containerUri)
        } else ""

        val binaryPath = when (commandType) {
            CommandType.Adb -> binaryManager.getAdbPath(containerPath)
            CommandType.Fastboot -> binaryManager.getFastbootPath(containerPath)
            CommandType.Shell -> "/system/bin/sh"
            CommandType.Auto -> {
                when {
                    command.startsWith("adb ") -> binaryManager.getAdbPath(containerPath)
                    command.startsWith("fastboot ") -> binaryManager.getFastbootPath(containerPath)
                    else -> "/system/bin/sh"
                }
            }
        }

        val args = mutableListOf(binaryPath)

        if (commandType != CommandType.Shell) {
            if (serial != null) {
                args.addAll(listOf("-s", serial))
            }
        }

        val cmdParts = command.trim().split("\\s+".toRegex())
        when (commandType) {
            CommandType.Auto -> {
                if (command.startsWith("adb ")) {
                    args.addAll(cmdParts.drop(1))
                } else if (command.startsWith("fastboot ")) {
                    args.addAll(cmdParts.drop(1))
                } else {
                    args.addAll(listOf("-c", containerPath, "-c", command))
                }
            }
            CommandType.Shell -> {
                args.addAll(listOf("-c", command))
            }
            else -> {
                args.addAll(cmdParts.drop(1))
            }
        }

        val env = mutableMapOf(
            "PATH" to "$containerPath/bin:${System.getenv("PATH")}",
            "HOME" to "$containerPath/home",
            "TERM" to "xterm-256color"
        )

        try {
            val process = ProcessBuilder(args)
                .directory(java.io.File("$containerPath/home"))
                .environment()
                .apply { putAll(env) }
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    emit(CommandOutput.Line(it, isError = false))
                    sessionManager.getSession(sessionId)?.write(it + "\r\n")
                }
            }

            val exitCode = process.waitFor()
            emit(CommandOutput.Exit(exitCode))

            commandHistoryDao.insert(
                CommandHistoryEntry(
                    sessionId = sessionId,
                    command = command
                )
            )
        } catch (e: Exception) {
            emit(CommandOutput.Line("Error: ${e.message}", isError = true))
            emit(CommandOutput.Exit(1))
        }
    }.flowOn(Dispatchers.IO)
}
