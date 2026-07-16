package com.pontifex.app.service.terminal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.pontifex.app.data.binary.BinaryManager
import com.termux.terminal.TerminalBuffer
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject

class PontifexTerminalSession @Inject constructor(
    @ApplicationContext private val context: Context,
    private val binaryManager: BinaryManager,
    val id: Int,
    val name: String,
    private val containerPath: String
) : TerminalSessionClient {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var termuxSession: TerminalSession

    private val _screen = MutableStateFlow<TerminalBuffer?>(null)
    val screen: StateFlow<TerminalBuffer?> = _screen.asStateFlow()

    private val _titleFlow = MutableStateFlow(name)
    val title: StateFlow<String> = _titleFlow.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val adbPath: String get() = binaryManager.getAdbPath(containerPath)
    private val fastbootPath: String get() = binaryManager.getFastbootPath(containerPath)

    fun start(columns: Int = 80, rows: Int = 24) {
        val shell = "/system/bin/sh"
        val args = arrayOf(shell)
        val env = buildEnv()
        val cwd = File(containerPath, "home").also { it.mkdirs() }.absolutePath

        termuxSession = TerminalSession(
            shell, cwd, args, env, rows, columns, this
        )
        _isRunning.value = true
    }

    private fun buildEnv(): Array<String> {
        val binDir = "$containerPath/bin"
        val existing = System.getenv("PATH") ?: ""
        return arrayOf(
            "PATH=$binDir:$existing",
            "HOME=$containerPath/home",
            "TMPDIR=$containerPath/tmp",
            "TERM=xterm-256color",
            "LANG=en_US.UTF-8",
            "ADB=$adbPath",
            "FASTBOOT=$fastbootPath"
        )
    }

    fun write(data: String) = termuxSession.write(data)

    fun write(data: ByteArray) = termuxSession.write(String(data))

    fun sendSignal(signal: Int) {
        termuxSession.sendHangupSignal()
    }

    fun resize(columns: Int, rows: Int) {
        termuxSession.updateSize(columns, rows)
    }

    fun destroy() {
        termuxSession.finishIfRunning()
        _isRunning.value = false
        scope.cancel()
    }

    fun getCwd(): String = try {
        termuxSession.cwd ?: "$containerPath/home"
    } catch (_: Exception) {
        "$containerPath/home"
    }

    override fun onTextChanged(changedSession: TerminalSession) {
        _screen.value = changedSession.emulator?.screen
    }

    override fun onTitleChanged(changedSession: TerminalSession) {
        _titleFlow.value = changedSession.title ?: name
    }

    override fun onSessionFinished(finishedSession: TerminalSession) {
        _isRunning.value = false
    }

    override fun onCopyTextToClipboard(session: TerminalSession, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Terminal Output", text)
        clipboard.setPrimaryClip(clip)
    }

    override fun onPasteTextFromClipboard(session: TerminalSession?) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val text = clip.getItemAt(0).coerceToText(context).toString()
            termuxSession.write(text)
        }
    }

    override fun onBell(session: TerminalSession) {
        // Vibrate on bell
    }

    override fun onColorsChanged(session: TerminalSession) {}

    override fun onTerminalCursorStateChange(state: Boolean) {}

    override fun setTerminalShellPid(session: TerminalSession, pid: Int) {}

    override fun getTerminalCursorStyle(): Int = TerminalEmulator.DEFAULT_TERMINAL_CURSOR_STYLE

    override fun logError(tag: String?, message: String?) {}
    override fun logWarn(tag: String?, message: String?) {}
    override fun logInfo(tag: String?, message: String?) {}
    override fun logDebug(tag: String?, message: String?) {}
    override fun logVerbose(tag: String?, message: String?) {}
    override fun logStackTraceWithMessage(tag: String?, message: String?, e: Exception?) {}
    override fun logStackTrace(tag: String?, e: Exception?) {}
}
