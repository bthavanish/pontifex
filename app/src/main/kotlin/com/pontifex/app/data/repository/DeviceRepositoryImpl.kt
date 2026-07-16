package com.pontifex.app.data.repository

import com.pontifex.app.data.db.dao.DeviceDao
import com.pontifex.app.data.db.entity.ConnectedDevice
import com.pontifex.app.domain.model.ConnectionType
import com.pontifex.app.domain.model.DeviceConnection
import com.pontifex.app.domain.model.DeviceState
import com.pontifex.app.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceRepositoryImpl @Inject constructor(
    private val deviceDao: DeviceDao
) : DeviceRepository {
    override fun getConnectedDevices(): Flow<List<DeviceConnection>> =
        deviceDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun addDevice(device: DeviceConnection) {
        deviceDao.upsert(device.toEntity())
    }

    override suspend fun removeDevice(serial: String) {
        deviceDao.delete(serial)
    }

    override fun getDeviceHistory(): Flow<List<DeviceConnection>> =
        deviceDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun syncDevices(devices: List<DeviceConnection>) {
        deviceDao.deleteAll()
        devices.forEach { device ->
            deviceDao.upsert(device.toEntity())
        }
    }

    private fun ConnectedDevice.toDomain() = DeviceConnection(
        serial = serial,
        name = name,
        connectionType = when (connectionType) {
            "Usb" -> ConnectionType.Usb
            "Self" -> ConnectionType.Self
            else -> {
                val parts = connectionType.split(":")
                if (parts.size == 3) {
                    ConnectionType.Wireless(parts[1], parts[2].toIntOrNull() ?: 5555)
                } else {
                    ConnectionType.Usb
                }
            }
        },
        state = when (state) {
            "Online" -> DeviceState.Online
            "Offline" -> DeviceState.Offline
            "Unauthorized" -> DeviceState.Unauthorized
            "Bootloader" -> DeviceState.Bootloader
            else -> DeviceState.Unknown
        }
    )

    private fun DeviceConnection.toEntity() = ConnectedDevice(
        serial = serial,
        name = name,
        connectionType = when (connectionType) {
            is ConnectionType.Usb -> "Usb"
            is ConnectionType.Self -> "Self"
            is ConnectionType.Wireless -> "Wireless:${connectionType.address}:${connectionType.port}"
        },
        state = state.name
    )
}
