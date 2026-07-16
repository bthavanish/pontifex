package com.pontifex.app.service.device

import com.pontifex.app.domain.model.DeviceConnection
import com.pontifex.app.domain.model.DeviceState
import com.pontifex.app.service.transport.UsbDeviceEvent
import com.pontifex.app.service.transport.UsbHostManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceMonitor @Inject constructor(
    private val deviceDetector: DeviceDetector,
    private val usbHostManager: UsbHostManager
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _devices = MutableStateFlow<List<DeviceConnection>>(emptyList())
    val devices: StateFlow<List<DeviceConnection>> = _devices.asStateFlow()

    init {
        scope.launch {
            val usbDevices = usbHostManager.observeUsbDevices()
                .map { event ->
                    when (event) {
                        is UsbDeviceEvent.Attached -> {
                            usbHostManager.requestPermission(event.device)
                            null
                        }
                        is UsbDeviceEvent.PermissionGranted -> {
                            deviceDetector.probeUsbDevice(event.device)
                        }
                        is UsbDeviceEvent.Detached -> {
                            DetachEvent(event.device.serialNumber ?: "")
                        }
                        is UsbDeviceEvent.PermissionDenied -> null
                    }
                }
                .filter { it != null }

            val wirelessPoll = tickerFlow(10_000)
                .flatMapLatest { deviceDetector.pollWirelessDevices() }

            merge(
                usbDevices,
                wirelessPoll
            ).scan(emptyList<DeviceConnection>()) { acc, event ->
                when (event) {
                    is DeviceConnection -> {
                        if (acc.none { it.serial == event.serial }) {
                            acc + event
                        } else {
                            acc.map { if (it.serial == event.serial) event else it }
                        }
                    }
                    is DetachEvent -> {
                        acc.filter { it.serial != event.serial }
                    }
                    else -> acc
                }
            }.collect { _devices.value = it }
        }
    }

    fun stop() {
        scope.cancel()
    }

    private fun tickerFlow(intervalMs: Long): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            kotlinx.coroutines.delay(intervalMs)
        }
    }

    private data class DetachEvent(val serial: String)
}
