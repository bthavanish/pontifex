package com.pontifex.app.data.binary

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChecksumVerifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val binaryManager: BinaryManager
) {
    private var cachedChecksums: Map<String, String>? = null

    suspend fun verifyAll(containerPath: String): Result<Unit> {
        return try {
            val checksums = loadChecksums()

            val adbFile = File("$containerPath/bin/adb")
            val fastbootFile = File("$containerPath/bin/fastboot")

            if (adbFile.exists()) {
                val adbHash = computeStreamingSha256(adbFile)
                val expectedAdb = checksums["adb"] ?: checksums["$containerPath/bin/adb"]
                if (expectedAdb != null && adbHash != expectedAdb) {
                    return Result.failure(
                        SecurityException("ADB checksum mismatch: expected $expectedAdb, got $adbHash")
                    )
                }
            }

            if (fastbootFile.exists()) {
                val fastbootHash = computeStreamingSha256(fastbootFile)
                val expectedFastboot = checksums["fastboot"] ?: checksums["$containerPath/bin/fastboot"]
                if (expectedFastboot != null && fastbootHash != expectedFastboot) {
                    return Result.failure(
                        SecurityException("Fastboot checksum mismatch: expected $expectedFastboot, got $fastbootHash")
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun loadChecksums(): Map<String, String> {
        cachedChecksums?.let { return it }

        val checksums = mutableMapOf<String, String>()
        try {
            context.assets.open("checksums.sha256").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.split("  ", limit = 2)
                    if (parts.size == 2) {
                        checksums[parts[1].trim()] = parts[0].trim()
                    }
                }
            }
        } catch (_: Exception) {
        }

        cachedChecksums = checksums
        return checksums
    }

    private fun computeStreamingSha256(file: File): String {
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
}
