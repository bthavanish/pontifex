package com.pontifex.app.data.binary

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContainerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isContainerValid(uri: String): Boolean {
        return try {
            val path = uri.removePrefix("file://")
            val dir = File(path)
            dir.exists() && dir.isDirectory && dir.canWrite()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun initializeContainer(uri: String): Result<Unit> {
        return try {
            val path = uri.removePrefix("file://")
            val root = File(path, "pontifex")

            val dirs = listOf("bin", "home", "tmp", "work", "sessions", "logs")
            dirs.forEach { dirName ->
                File(root, dirName).mkdirs()
            }

            val tmpDir = File(root, "tmp")
            tmpDir.setWritable(true)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getContainerPath(uri: String): String {
        return "${uri.removePrefix("file://")}/pontifex"
    }

    fun getBinDir(containerPath: String): File = File(containerPath, "bin")
    fun getHomeDir(containerPath: String): File = File(containerPath, "home")
    fun getTmpDir(containerPath: String): File = File(containerPath, "tmp")
    fun getWorkDir(containerPath: String): File = File(containerPath, "work")
}
