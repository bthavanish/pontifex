package com.pontifex.app.service.terminal

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class ShellProcess(
    private val containerPath: String
) {
    private var process: Process? = null
    private var outputStream: java.io.OutputStream? = null

    fun start(): Process {
        val env = ShellEnvironment(containerPath)
        val homeDir = File("$containerPath/home")

        if (!homeDir.exists()) {
            homeDir.mkdirs()
        }

        val processBuilder = ProcessBuilder(listOf("/system/bin/sh"))
            .directory(homeDir)
            .apply {
                environment().putAll(env.buildEnvironment())
                redirectErrorStream(true)
            }

        val proc = processBuilder.start()
        process = proc
        outputStream = proc.outputStream
        return proc
    }

    fun sendCommand(command: String) {
        outputStream?.let { stream ->
            stream.write((command + "\n").toByteArray())
            stream.flush()
        }
    }

    fun sendSignal(signal: Int) {
        process?.let { proc ->
            try {
                val pidField = proc.javaClass.getDeclaredField("pid")
                pidField.isAccessible = true
                val pid = pidField.getInt(proc)
                Runtime.getRuntime().exec(arrayOf("kill", "-$signal", pid.toString()))
            } catch (_: Exception) {
            }
        }
    }

    fun destroy() {
        outputStream?.close()
        process?.destroy()
        process?.waitFor()
    }
}
