package com.pontifex.app.data.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object SettingsKeys {
    val CONTAINER_URI = stringPreferencesKey("container_uri")
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    val FONT_SIZE = intPreferencesKey("font_size")
    val FONT_FAMILY = stringPreferencesKey("font_family")
    val COLOR_SCHEME = stringPreferencesKey("color_scheme")
    val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
    val AMOLED_BLACK = booleanPreferencesKey("amoled_black")
    val DARK_MODE = booleanPreferencesKey("dark_mode")
    val SCROLLBACK_LINES = intPreferencesKey("scrollback_lines")
    val SHOW_EXTRA_KEYS = booleanPreferencesKey("show_extra_keys")
    val CURSOR_STYLE = stringPreferencesKey("cursor_style")
    val CURSOR_BLINK = booleanPreferencesKey("cursor_blink")
    val DEFAULT_SHELL = stringPreferencesKey("default_shell")
    val BELL_SOUND = booleanPreferencesKey("bell_sound")
    val VIBRATE_ON_BELL = booleanPreferencesKey("vibrate_on_bell")
    val DEFAULT_ADB_PORT = intPreferencesKey("default_adb_port")
    val WIRELESS_SCAN_TIMEOUT = intPreferencesKey("wireless_scan_timeout")
    val KEEP_ALIVE_INTERVAL = stringPreferencesKey("keep_alive_interval")
    val AUTO_RECONNECT = booleanPreferencesKey("auto_reconnect")
    val BINARY_SOURCE = stringPreferencesKey("binary_source")
    val AUTO_CHECK_UPDATES = booleanPreferencesKey("auto_check_updates")
}
