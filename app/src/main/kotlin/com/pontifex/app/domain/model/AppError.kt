package com.pontifex.app.domain.model

sealed interface AppError {
    val message: String

    data class BinaryIntegrity(val expected: String, val actual: String) : AppError {
        override val message: String get() = if (actual.isNotBlank()) actual else "Binary integrity check failed"
    }
    data class ContainerInit(val reason: String) : AppError {
        override val message: String get() = if (reason.isNotBlank()) reason else "Failed to initialize container"
    }
    data class UsbPermission(val deviceName: String) : AppError {
        override val message: String get() = "USB permission denied for $deviceName"
    }
    data class DeviceOffline(val serial: String) : AppError {
        override val message: String get() = "Device $serial is offline"
    }
    data class ConnectionFailed(val address: String, val reason: String) : AppError {
        override val message: String get() = "Connection to $address failed: $reason"
    }
    data class ProcessError(val exitCode: Int, val output: String) : AppError {
        override val message: String get() = "Process exited with code $exitCode"
    }
    data class StorageError(val reason: String) : AppError {
        override val message: String get() = if (reason.isNotBlank()) reason else "Storage error"
    }
    data class ShellNotFound(val path: String) : AppError {
        override val message: String get() = "Shell not found at $path"
    }
}
