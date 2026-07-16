package com.pontifex.app.service.update

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateChecker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: OkHttpClient
) {
    data class UpdateInfo(
        val versionName: String,
        val downloadUrl: String,
        val releaseNotes: String
    )

    suspend fun checkForUpdates(): Result<UpdateInfo?> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url("https://api.github.com/repos/pontifex-app/pontifex/releases/latest")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.success(null)
                }

                val body = response.body?.string() ?: return@withContext Result.success(null)

                val versionName = Regex("\"tag_name\"\\s*:\\s*\"([^\"]+)\"")
                    .find(body)?.groupValues?.get(1) ?: return@withContext Result.success(null)

                val downloadUrl = Regex("\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"")
                    .find(body)?.groupValues?.get(1) ?: ""

                val releaseNotes = Regex("\"body\"\\s*:\\s*\"([^\"]+)\"")
                    .find(body)?.groupValues?.get(1) ?: ""

                Result.success(
                    UpdateInfo(
                        versionName = versionName,
                        downloadUrl = downloadUrl,
                        releaseNotes = releaseNotes
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
