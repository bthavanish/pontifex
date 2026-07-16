package com.pontifex.app.service.terminal

import java.io.File

class ShellEnvironment(private val containerPath: String) {
    fun buildEnvironment(): Map<String, String> = mapOf(
        "HOME" to "$containerPath/home",
        "TMPDIR" to "$containerPath/tmp",
        "PATH" to "$containerPath/bin:${System.getenv("PATH")}",
        "PONTEX_HOME" to containerPath,
        "SHELL" to detectShell(),
        "TERM" to "xterm-256color",
        "LANG" to "en_US.UTF-8"
    )

    private fun detectShell(): String {
        return when {
            File("$containerPath/bin/bash").exists() -> "$containerPath/bin/bash"
            else -> "/system/bin/sh"
        }
    }
}
