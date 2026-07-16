package com.pontifex.app.domain.model

sealed interface ConnectionType {
    data object Usb : ConnectionType
    data class Wireless(val address: String, val port: Int) : ConnectionType
    data object Self : ConnectionType
}
