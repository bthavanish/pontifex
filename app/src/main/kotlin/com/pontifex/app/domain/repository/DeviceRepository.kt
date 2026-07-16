package com.pontifex.app.domain.repository

import com.pontifex.app.domain.model.DeviceConnection
import kotlinx.coroutines.flow.Flow

interface DeviceRepository {
    fun getConnectedDevices(): Flow<List<DeviceConnection>>
    suspend fun addDevice(device: DeviceConnection)
    suspend fun removeDevice(serial: String)
    fun getDeviceHistory(): Flow<List<DeviceConnection>>
}
