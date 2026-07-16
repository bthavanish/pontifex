package com.pontifex.app.data.binary

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
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
            val path = resolveToPath(uri) ?: return false
            val dir = File(path, "pontifex")
            dir.exists() && dir.isDirectory && dir.canWrite()
        } catch (_: Exception) {
            false
        }
    }

    suspend fun initializeContainer(uri: String): Result<Unit> {
        return try {
            val path = resolveToPath(uri)
                ?: return Result.failure(Exception("Could not resolve directory path. Please choose a local directory."))
            val root = File(path, "pontifex")

            val dirs = listOf("bin", "home", "tmp", "work", "sessions", "logs")
            dirs.forEach { dirName ->
                val dir = File(root, dirName)
                if (!dir.mkdirs() && !dir.exists()) {
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
        val path = resolveToPath(uri)
            ?: throw IllegalArgumentException("Could not resolve path for URI: $uri")
        return "$path/pontifex"
    }

    fun getBinDir(containerPath: String): File = File(containerPath, "bin")
    fun getHomeDir(containerPath: String): File = File(containerPath, "home")
    fun getTmpDir(containerPath: String): File = File(containerPath, "tmp")
    fun getWorkDir(containerPath: String): File = File(containerPath, "work")

    /**
     * Resolve a SAF tree URI or file URI to an actual filesystem path.
     * Handles content:// URIs from ACTION_OPEN_DOCUMENT_TREE and file:// URIs.
     */
    private fun resolveToPath(uri: String): String? {
        if (uri.startsWith("file://")) {
            return uri.removePrefix("file://")
        }

        return try {
            val treeUri = Uri.parse(uri)
            val docId = DocumentsContract.getTreeDocumentId(treeUri)
            val split = docId.split(":")

            if (split.size >= 2) {
                val type = split[0]
                val relativePath = split.subList(1, split.size).joinToString(":")

                when (type) {
                    "primary" -> {
                        val base = Environment.getExternalStorageDirectory().absolutePath
                        if (relativePath.isEmpty()) base else "$base/$relativePath"
                    }
                    "external" -> {
                        val base = Environment.getExternalStorageDirectory().absolutePath
                        if (relativePath.isEmpty()) base else "$base/$relativePath"
                    }
                    else -> {
                        null
                    }
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
