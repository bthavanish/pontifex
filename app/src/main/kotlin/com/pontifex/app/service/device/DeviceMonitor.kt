package com.pontifex.app.service.device

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceDetector: DeviceDetector
) {
    fun monitorDevices(intervalMs: Long = 5000): Flow<List<String>> = flow {
        while (true) {
            deviceDetector.detectDevices().collect { devices ->
                emit(devices)
            }
            kotlinx.coroutines.delay(intervalMs)
        }
    }
}
