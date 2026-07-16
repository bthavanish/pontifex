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

    suspend fun extractBinaries(containerPath: String): Result<Float> {
        return try {
            val abi = getAbi()
            val binDir = File(containerPath, "bin").also { it.mkdirs() }

            listOf("adb", "fastboot").forEachIndexed { index, binary ->
                val assetPath = "bin/$abi/$binary"
                val target = File(binDir, binary)

                val assetSize = try {
                    context.assets.openFd(assetPath).use { it.length }
                } catch (_: Exception) {
                    0L
                }

                if (!target.exists() || target.length() != assetSize) {
                    context.assets.open(assetPath).use { input ->
                        target.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }

                target.setReadable(true, false)
                target.setExecutable(true, false)
            }

            Result.success(1f)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getBinaryFiles(containerPath: String): List<Pair<String, String>> {
        val abi = getAbi()
        return listOf("adb", "fastboot").map { binary ->
            val assetPath = "bin/$abi/$binary"
            val target = File("$containerPath/bin", binary)
            assetPath to target.absolutePath
        }
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
