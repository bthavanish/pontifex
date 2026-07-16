package com.pontifex.app.domain.model

data class DeviceConnection(
    val serial: String,
    val name: String,
    val connectionType: ConnectionType,
    val state: DeviceState
)

enum class DeviceState {
    Online,
    Offline,
    Unauthorized,
    Bootloader,
    Unknown
}
