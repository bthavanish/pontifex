package com.pontifex.app.domain.repository

import com.pontifex.app.domain.model.ContainerState
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getContainerUri(): Flow<String>
    suspend fun setContainerUri(uri: String)
    fun isOnboardingComplete(): Flow<Boolean>
    suspend fun setOnboardingComplete(complete: Boolean)
    fun getFontSize(): Flow<Int>
    suspend fun setFontSize(size: Int)
    fun getFontFamily(): Flow<String>
    suspend fun setFontFamily(family: String)
    fun getColorScheme(): Flow<String>
    suspend fun setColorScheme(scheme: String)
    fun isDynamicColor(): Flow<Boolean>
    suspend fun setDynamicColor(enabled: Boolean)
    fun isAmoledBlack(): Flow<Boolean>
    suspend fun setAmoledBlack(enabled: Boolean)
    fun isDarkMode(): Flow<Boolean>
    suspend fun setDarkMode(dark: Boolean)
    fun isSystemDarkMode(): Flow<Boolean>
    fun getScrollbackLines(): Flow<Int>
    suspend fun setScrollbackLines(lines: Int)
    fun isShowExtraKeys(): Flow<Boolean>
    suspend fun setShowExtraKeys(show: Boolean)
    fun getCursorStyle(): Flow<String>
    suspend fun setCursorStyle(style: String)
    fun isCursorBlink(): Flow<Boolean>
    suspend fun setCursorBlink(enabled: Boolean)
    fun getDefaultShell(): Flow<String>
    suspend fun setDefaultShell(shell: String)
    fun isBellSound(): Flow<Boolean>
    suspend fun setBellSound(enabled: Boolean)
    fun isVibrateOnBell(): Flow<Boolean>
    suspend fun setVibrateOnBell(enabled: Boolean)
    fun getDefaultAdbPort(): Flow<Int>
    suspend fun setDefaultAdbPort(port: Int)
    fun getWirelessScanTimeout(): Flow<Int>
    suspend fun setWirelessScanTimeout(timeout: Int)
    fun getKeepAliveInterval(): Flow<String>
    suspend fun setKeepAliveInterval(interval: String)
    fun isAutoReconnect(): Flow<Boolean>
    suspend fun setAutoReconnect(enabled: Boolean)
    fun getBinarySource(): Flow<String>
    suspend fun setBinarySource(source: String)
    fun isAutoCheckUpdates(): Flow<Boolean>
    suspend fun setAutoCheckUpdates(enabled: Boolean)
}
