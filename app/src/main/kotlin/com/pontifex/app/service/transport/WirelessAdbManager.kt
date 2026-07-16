package com.pontifex.app.service.transport

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.domain.model.ConnectionType
import com.pontifex.app.domain.model.DeviceConnection
import com.pontifex.app.domain.model.DeviceState
import com.pontifex.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WirelessAdbManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val binaryManager: BinaryManager,
    private val containerManager: ContainerManager,
    private val settingsRepository: SettingsRepository
) {
    private val nsdManager = context.getSystemService(NsdManager::class.java)

    private suspend fun getAdbPath(): String {
        val containerUri = settingsRepository.getContainerUri().first()
        return if (!containerUri.isNullOrBlank()) {
            val containerPath = containerManager.getContainerPath(containerUri)
            binaryManager.getAdbPath(containerPath)
        } else {
            "adb"
        }
    }

    fun discoverServices(): Flow<NsdServiceInfo> = callbackFlow {
        val listener = object : NsdManager.DiscoveryListener {
            override fun onServiceFound(info: NsdServiceInfo) {
                nsdManager.resolveService(info, object : NsdManager.ResolveListener {
                    override fun onServiceResolved(resolved: NsdServiceInfo) {
                        trySend(resolved)
                    }

                    override fun onResolveFailed(info: NsdServiceInfo, code: Int) {
                    }
                })
            }

            override fun onDiscoveryStarted(type: String) {}
            override fun onDiscoveryStopped(type: String) {}
            override fun onServiceLost(info: NsdServiceInfo) {}
            override fun onStartDiscoveryFailed(type: String, code: Int) { close() }
            override fun onStopDiscoveryFailed(type: String, code: Int) { close() }
        }

        nsdManager.discoverServices(
            "_adb-tls-connect._tcp.",
            NsdManager.PROTOCOL_DNS_SD,
            listener
        )

        awaitClose {
            try {
                nsdManager.stopServiceDiscovery(listener)
            } catch (_: Exception) {
            }
        }
    }

    suspend fun pairDevice(ip: String, port: Int, code: String): Result<Boolean> {
        return try {
            val adbPath = getAdbPath()
            val process = ProcessBuilder(
                listOf(adbPath, "pair", "$ip:$port", code)
            ).redirectErrorStream(true).start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()

            if (output.contains("Successfully paired")) {
                Result.success(true)
            } else {
                Result.failure(Exception("Pairing failed: $output"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connectDevice(ip: String, port: Int): Result<DeviceConnection> {
        return try {
            val adbPath = getAdbPath()
            val process = ProcessBuilder(
                listOf(adbPath, "connect", "$ip:$port")
            ).redirectErrorStream(true).start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()
            process.waitFor()

            if (output.contains("connected")) {
                val serial = output.substringBefore(" ").substringAfterLast(" ")
                Result.success(
                    DeviceConnection(
                        serial = serial.ifBlank { "$ip:$port" },
                        name = "Device at $ip:$port",
                        connectionType = ConnectionType.Wireless(ip, port),
                        state = DeviceState.Online
                    )
                )
            } else {
                Result.failure(Exception("Connection failed: $output"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
