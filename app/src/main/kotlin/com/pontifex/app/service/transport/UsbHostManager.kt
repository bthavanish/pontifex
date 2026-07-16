package com.pontifex.app.service.transport

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsbHostManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usbManager = context.getSystemService(UsbManager::class.java)

    fun observeUsbDevices(): Flow<UsbDeviceEvent> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE, UsbDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                } ?: return

                when (intent.action) {
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                        if (device.isAdbDevice()) trySend(UsbDeviceEvent.Attached(device))
                    }
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        trySend(UsbDeviceEvent.Detached(device))
                    }
                    ACTION_USB_PERMISSION -> {
                        val granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        if (granted) {
                            trySend(UsbDeviceEvent.PermissionGranted(device))
                        } else {
                            trySend(UsbDeviceEvent.PermissionDenied(device))
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
            addAction(ACTION_USB_PERMISSION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }

        awaitClose { context.unregisterReceiver(receiver) }
    }

    fun requestPermission(device: UsbDevice) {
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val permissionIntent = PendingIntent.getBroadcast(
            context, 0, Intent(ACTION_USB_PERMISSION), flags
        )
        usbManager.requestPermission(device, permissionIntent)
    }

    fun openConnection(device: UsbDevice): UsbDeviceConnection? {
        return usbManager.openDevice(device)
    }

    private fun UsbDevice.isAdbDevice(): Boolean {
        return (0 until interfaceCount).any { i ->
            getInterface(i).let { iface ->
                iface.interfaceClass == 0xFF &&
                    iface.interfaceSubclass == 0x42 &&
                    iface.interfaceProtocol == 0x01
            }
        }
    }

    companion object {
        private const val ACTION_USB_PERMISSION = "com.pontifex.app.USB_PERMISSION"
    }
}

sealed interface UsbDeviceEvent {
    data class Attached(val device: UsbDevice) : UsbDeviceEvent
    data class Detached(val device: UsbDevice) : UsbDeviceEvent
    data class PermissionGranted(val device: UsbDevice) : UsbDeviceEvent
    data class PermissionDenied(val device: UsbDevice) : UsbDeviceEvent
}
