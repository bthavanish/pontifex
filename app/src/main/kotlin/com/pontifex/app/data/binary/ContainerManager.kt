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
    private val containerRoot: File
        get() = File(context.getExternalFilesDir(null), "pontifex")

    fun isContainerValid(uri: String): Boolean {
        return try {
            containerRoot.exists() && containerRoot.isDirectory && containerRoot.canWrite()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun initializeContainer(uri: String): Result<Unit> {
        return try {
            val root = containerRoot
            if (!root.exists() && !root.mkdirs()) {
                return Result.failure(Exception("Failed to create container directory"))
            }

            val dirs = listOf("bin", "home", "tmp", "work", "sessions", "logs")
            dirs.forEach { dirName ->
                val dir = File(root, dirName)
                if (!dir.exists() && !dir.mkdirs()) {
                    return Result.failure(Exception("Failed to create directory: $dirName"))
                }
            }

            File(root, "tmp").setWritable(true)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getContainerPath(uri: String): String {
        return containerRoot.absolutePath
    }

    fun getBinDir(containerPath: String): File = File(containerPath, "bin")
    fun getHomeDir(containerPath: String): File = File(containerPath, "home")
    fun getTmpDir(containerPath: String): File = File(containerPath, "tmp")
    fun getWorkDir(containerPath: String): File = File(containerPath, "work")
}
