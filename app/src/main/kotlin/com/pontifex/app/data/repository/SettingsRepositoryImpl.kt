package com.pontifex.app.data.repository

import com.pontifex.app.data.datastore.SettingsDataStore
import com.pontifex.app.data.datastore.SettingsKeys
import com.pontifex.app.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {
    override fun getContainerUri(): Flow<String> =
        dataStore.getString(SettingsKeys.CONTAINER_URI, "")

    override suspend fun setContainerUri(uri: String) =
        dataStore.setString(SettingsKeys.CONTAINER_URI, uri)

    override fun isOnboardingComplete(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.ONBOARDING_COMPLETE, false)

    override suspend fun setOnboardingComplete(complete: Boolean) =
        dataStore.setBoolean(SettingsKeys.ONBOARDING_COMPLETE, complete)

    override fun getFontSize(): Flow<Int> =
        dataStore.getInt(SettingsKeys.FONT_SIZE, 14)

    override suspend fun setFontSize(size: Int) =
        dataStore.setInt(SettingsKeys.FONT_SIZE, size)

    override fun getFontFamily(): Flow<String> =
        dataStore.getString(SettingsKeys.FONT_FAMILY, "JetBrains Mono")

    override suspend fun setFontFamily(family: String) =
        dataStore.setString(SettingsKeys.FONT_FAMILY, family)

    override fun getColorScheme(): Flow<String> =
        dataStore.getString(SettingsKeys.COLOR_SCHEME, "Default")

    override suspend fun setColorScheme(scheme: String) =
        dataStore.setString(SettingsKeys.COLOR_SCHEME, scheme)

    override fun isDynamicColor(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.DYNAMIC_COLOR, true)

    override suspend fun setDynamicColor(enabled: Boolean) =
        dataStore.setBoolean(SettingsKeys.DYNAMIC_COLOR, enabled)

    override fun isAmoledBlack(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.AMOLED_BLACK, false)

    override suspend fun setAmoledBlack(enabled: Boolean) =
        dataStore.setBoolean(SettingsKeys.AMOLED_BLACK, enabled)

    override fun isDarkMode(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.DARK_MODE, true)

    override suspend fun setDarkMode(dark: Boolean) =
        dataStore.setBoolean(SettingsKeys.DARK_MODE, dark)

    override fun isSystemDarkMode(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.SYSTEM_DARK_MODE, true)

    override fun getScrollbackLines(): Flow<Int> =
        dataStore.getInt(SettingsKeys.SCROLLBACK_LINES, 5000)

    override suspend fun setScrollbackLines(lines: Int) =
        dataStore.setInt(SettingsKeys.SCROLLBACK_LINES, lines)

    override fun isShowExtraKeys(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.SHOW_EXTRA_KEYS, true)

    override suspend fun setShowExtraKeys(show: Boolean) =
        dataStore.setBoolean(SettingsKeys.SHOW_EXTRA_KEYS, show)

    override fun getCursorStyle(): Flow<String> =
        dataStore.getString(SettingsKeys.CURSOR_STYLE, "Block")

    override suspend fun setCursorStyle(style: String) =
        dataStore.setString(SettingsKeys.CURSOR_STYLE, style)

    override fun isCursorBlink(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.CURSOR_BLINK, true)

    override suspend fun setCursorBlink(enabled: Boolean) =
        dataStore.setBoolean(SettingsKeys.CURSOR_BLINK, enabled)

    override fun getDefaultShell(): Flow<String> =
        dataStore.getString(SettingsKeys.DEFAULT_SHELL, "sh")

    override suspend fun setDefaultShell(shell: String) =
        dataStore.setString(SettingsKeys.DEFAULT_SHELL, shell)

    override fun isBellSound(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.BELL_SOUND, true)

    override suspend fun setBellSound(enabled: Boolean) =
        dataStore.setBoolean(SettingsKeys.BELL_SOUND, enabled)

    override fun isVibrateOnBell(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.VIBRATE_ON_BELL, true)

    override suspend fun setVibrateOnBell(enabled: Boolean) =
        dataStore.setBoolean(SettingsKeys.VIBRATE_ON_BELL, enabled)

    override fun getDefaultAdbPort(): Flow<Int> =
        dataStore.getInt(SettingsKeys.DEFAULT_ADB_PORT, 5555)

    override suspend fun setDefaultAdbPort(port: Int) =
        dataStore.setInt(SettingsKeys.DEFAULT_ADB_PORT, port)

    override fun getWirelessScanTimeout(): Flow<Int> =
        dataStore.getInt(SettingsKeys.WIRELESS_SCAN_TIMEOUT, 10)

    override suspend fun setWirelessScanTimeout(timeout: Int) =
        dataStore.setInt(SettingsKeys.WIRELESS_SCAN_TIMEOUT, timeout)

    override fun getKeepAliveInterval(): Flow<String> =
        dataStore.getString(SettingsKeys.KEEP_ALIVE_INTERVAL, "30s")

    override suspend fun setKeepAliveInterval(interval: String) =
        dataStore.setString(SettingsKeys.KEEP_ALIVE_INTERVAL, interval)

    override fun isAutoReconnect(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.AUTO_RECONNECT, true)

    override suspend fun setAutoReconnect(enabled: Boolean) =
        dataStore.setBoolean(SettingsKeys.AUTO_RECONNECT, enabled)

    override fun getBinarySource(): Flow<String> =
        dataStore.getString(SettingsKeys.BINARY_SOURCE, "Bundled")

    override suspend fun setBinarySource(source: String) =
        dataStore.setString(SettingsKeys.BINARY_SOURCE, source)

    override fun isAutoCheckUpdates(): Flow<Boolean> =
        dataStore.getBoolean(SettingsKeys.AUTO_CHECK_UPDATES, true)

    override suspend fun setAutoCheckUpdates(enabled: Boolean) =
        dataStore.setBoolean(SettingsKeys.AUTO_CHECK_UPDATES, enabled)
}
