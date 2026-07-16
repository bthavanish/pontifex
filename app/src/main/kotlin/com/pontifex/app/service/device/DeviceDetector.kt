package com.pontifex.app.service.device

import android.content.Context
import android.hardware.usb.UsbDevice
import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.domain.model.ConnectionType
import com.pontifex.app.domain.model.DeviceConnection
import com.pontifex.app.domain.model.DeviceState
import com.pontifex.app.domain.repository.SettingsRepository
import com.pontifex.app.service.transport.UsbHostManager
import com.pontifex.app.service.transport.WirelessAdbManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val binaryManager: BinaryManager,
    private val usbHostManager: UsbHostManager,
    private val wirelessAdbManager: WirelessAdbManager,
    private val settingsRepository: SettingsRepository,
    private val containerManager: ContainerManager
) {
    private suspend fun getAdbPath(): String {
        val containerUri = settingsRepository.getContainerUri().first()
        return if (!containerUri.isNullOrBlank()) {
            val containerPath = containerManager.getContainerPath(containerUri)
            binaryManager.getAdbPath(containerPath)
        } else {
            "adb"
        }
    }

    fun probeUsbDevice(device: UsbDevice): DeviceConnection {
        val serial = device.serialNumber ?: "usb:${device.deviceId}"
        val name = listOfNotNull(device.manufacturerName, device.productName)
            .joinToString(" ")
            .ifBlank { "USB Device" }

        return DeviceConnection(
            serial = serial,
            name = name,
            connectionType = ConnectionType.Usb,
            state = DeviceState.Online
        )
    }

    fun pollWirelessDevices(): Flow<List<DeviceConnection>> = flow {
        try {
            val adbPath = getAdbPath()
            val process = ProcessBuilder(listOf(adbPath, "devices"))
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val lines = reader.readLines()
            process.waitFor()

            val devices = lines.drop(1)
                .filter { line ->
                    line.contains("\tdevice") ||
                    line.contains("\tunauthorized") ||
                    line.contains("\toffline")
                }
                .map { line ->
                    val parts = line.split("\t")
                    val serial = parts.firstOrNull() ?: ""
                    val state = when (parts.getOrNull(1)) {
                        "device" -> DeviceState.Online
                        "unauthorized" -> DeviceState.Unauthorized
                        "offline" -> DeviceState.Offline
                        "bootloader" -> DeviceState.Bootloader
                        else -> DeviceState.Unknown
                    }
                    DeviceConnection(
                        serial = serial,
                        name = "Device $serial",
                        connectionType = ConnectionType.Wireless(
                            address = serial.substringBefore(":"),
                            port = serial.substringAfter(":").toIntOrNull() ?: 5555
                        ),
                        state = state
                    )
                }

            emit(devices)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }.flowOn(Dispatchers.IO)
}
