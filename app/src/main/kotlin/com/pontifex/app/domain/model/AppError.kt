package com.pontifex.app.domain.model

sealed interface AppError {
    data class BinaryIntegrity(val expected: String, val actual: String) : AppError
    data class ContainerInit(val reason: String) : AppError
    data class UsbPermission(val deviceName: String) : AppError
    data class DeviceOffline(val serial: String) : AppError
    data class ConnectionFailed(val address: String, val reason: String) : AppError
    data class ProcessError(val exitCode: Int, val output: String) : AppError
    data class StorageError(val reason: String) : AppError
    data class ShellNotFound(val path: String) : AppError
}
