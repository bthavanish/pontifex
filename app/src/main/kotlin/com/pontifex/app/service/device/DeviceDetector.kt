package com.pontifex.app.service.device

import android.content.Context
import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceDetector @Inject constructor(
    @ApplicationContext private val context: Context,
    private val binaryManager: BinaryManager,
    private val containerManager: ContainerManager,
    private val settingsRepository: SettingsRepository
) {
    fun detectDevices(): kotlinx.coroutines.flow.Flow<List<String>> = kotlinx.coroutines.flow.flow {
        try {
            val containerUri = settingsRepository.getContainerUri().first()
            val adbPath = if (!containerUri.isNullOrBlank()) {
                val containerPath = containerManager.getContainerPath(containerUri)
                binaryManager.getAdbPath(containerPath)
            } else {
                "adb"
            }

            val process = ProcessBuilder(listOf(adbPath, "devices"))
                .redirectErrorStream(true)
                .start()

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val lines = reader.readLines()
            process.waitFor()

            val devices = lines.drop(1)
                .filter { it.contains("\tdevice") }
                .map { it.split("\t").first() }

            emit(devices)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }
}
