package com.pontifex.app.service.terminal

import com.pontifex.app.domain.model.StyledSegment
import com.pontifex.app.domain.model.TerminalLine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class TerminalSession(
    val id: Int,
    val name: String,
    private val containerPath: String
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var shellProcess: ShellProcess? = null
    private var readJob: Job? = null

    private val _outputLines = MutableSharedFlow<TerminalLine>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val outputLines: SharedFlow<TerminalLine> = _outputLines.asSharedFlow()

    private val _scrollback = MutableStateFlow<List<TerminalLine>>(emptyList())
    val scrollback: StateFlow<List<TerminalLine>> = _scrollback.asStateFlow()

    private val scrollbackBuffer = ArrayDeque<TerminalLine>(5000)
    private var maxScrollbackLines = 5000

    private val ansiParser = AnsiParser()

    fun start(maxLines: Int = 5000) {
        maxScrollbackLines = maxLines
        shellProcess = ShellProcess(containerPath)
        val process = shellProcess?.start() ?: return

        val reader = BufferedReader(InputStreamReader(process.inputStream))
        readJob = scope.launch {
            try {
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    line?.let { rawLine ->
                        val parsed = ansiParser.parse(rawLine)
                        val terminalLine = TerminalLine(segments = parsed)
                        addToScrollback(terminalLine)
                        _outputLines.emit(terminalLine)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun sendCommand(command: String) {
        shellProcess?.sendCommand(command)
        val promptLine = TerminalLine(
            segments = listOf(
                StyledSegment(
                    text = "$ ",
                    foreground = androidx.compose.ui.graphics.Color(0xFF4CAF50)
                ),
                StyledSegment(text = command)
            ),
            isPrompt = true
        )
        addToScrollback(promptLine)
    }

    fun sendInterrupt() {
        shellProcess?.sendSignal(2)
    }

    fun resizeScrollback(newMaxLines: Int) {
        maxScrollbackLines = newMaxLines
        while (scrollbackBuffer.size > maxScrollbackLines) {
            scrollbackBuffer.removeFirst()
        }
        _scrollback.value = scrollbackBuffer.toList()
    }

    private fun addToScrollback(line: TerminalLine) {
        scrollbackBuffer.add(line)
        while (scrollbackBuffer.size > maxScrollbackLines) {
            scrollbackBuffer.removeFirst()
        }
        _scrollback.value = scrollbackBuffer.toList()
    }

    fun destroy() {
        readJob?.cancel()
        shellProcess?.destroy()
        scope.cancel()
    }
}
