package com.pontifex.app.domain.usecase

import com.pontifex.app.domain.model.ConnectionType
import com.pontifex.app.domain.model.DeviceConnection
import com.pontifex.app.domain.model.DeviceState
import com.pontifex.app.domain.repository.DeviceRepository
import com.pontifex.app.service.transport.WirelessAdbManager
import javax.inject.Inject

class ConnectDeviceUseCase @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val wirelessAdbManager: WirelessAdbManager
) {
    suspend operator fun invoke(
        ip: String,
        port: Int
    ): Result<DeviceConnection> {
        val result = wirelessAdbManager.connectDevice(ip, port)
        return result.onSuccess { connection ->
            deviceRepository.addDevice(connection)
        }
    }

    suspend fun pairDevice(
        ip: String,
        port: Int,
        code: String
    ): Result<Boolean> {
        return wirelessAdbManager.pairDevice(ip, port, code)
    }
}
