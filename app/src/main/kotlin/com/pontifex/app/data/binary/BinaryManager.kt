package com.pontifex.app.data.binary

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BinaryManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getAbi(): String {
        val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"
        return when (abi) {
            "arm64-v8a", "armeabi-v7a", "x86_64" -> abi
            else -> "arm64-v8a"
        }
    }

    suspend fun extractBinaries(containerUri: String): Result<Unit> {
        return try {
            val abi = getAbi()
            val containerPath = containerUri.removePrefix("file://")

            val binDir = File(containerPath, "bin")
            binDir.mkdirs()

            extractAsset("bin/$abi/adb", File(binDir, "adb"))
            extractAsset("bin/$abi/fastboot", File(binDir, "fastboot"))

            setExecutable(File(binDir, "adb"))
            setExecutable(File(binDir, "fastboot"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractAsset(assetPath: String, target: File) {
        context.assets.open(assetPath).use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun setExecutable(file: File) {
        file.setReadable(true)
        file.setExecutable(true)
    }

    fun computeSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { stream ->
            val buffer = ByteArray(8192)
            var read: Int
            while (stream.read(buffer).also { read = it } != -1) {
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    fun getAdbPath(containerPath: String): String = "$containerPath/bin/adb"
    fun getFastbootPath(containerPath: String): String = "$containerPath/bin/fastboot"
}
