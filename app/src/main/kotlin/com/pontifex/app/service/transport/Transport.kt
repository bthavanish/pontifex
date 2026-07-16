package com.pontifex.app.service.transport

import com.pontifex.app.domain.model.ConnectionType
import com.pontifex.app.domain.model.DeviceConnection
import com.pontifex.app.domain.model.DeviceState

sealed interface Transport {
    data class Usb(val fd: Int) : Transport
    data class Tcp(val address: String, val port: Int) : Transport
}
