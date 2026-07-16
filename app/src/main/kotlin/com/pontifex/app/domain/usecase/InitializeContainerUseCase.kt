package com.pontifex.app.domain.usecase

import android.content.Context
import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ChecksumVerifier
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.domain.model.AppError
import com.pontifex.app.domain.model.ContainerState
import com.pontifex.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.io.File
import javax.inject.Inject

class InitializeContainerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val containerManager: ContainerManager,
    private val binaryManager: BinaryManager,
    private val checksumVerifier: ChecksumVerifier,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(): Result<ContainerState> {
        val existingUri = settingsRepository.getContainerUri().first()

        if (!existingUri.isNullOrBlank()) {
            return if (containerManager.isContainerValid(existingUri)) {
                Result.success(ContainerState.Ready(existingUri))
            } else {
                Result.failure(Exception("Container URI no longer valid"))
            }
        }

        return Result.success(ContainerState.Uninitialized)
    }

    suspend fun initializeAtUri(uri: String): Result<ContainerState> {
        val initResult = containerManager.initializeContainer(uri)
        if (initResult.isFailure) {
            return Result.failure(
                Exception("Container init failed: ${initResult.exceptionOrNull()?.message ?: "Unknown error"}")
            )
        }

        val extractResult = binaryManager.extractBinaries(uri)
        if (extractResult.isFailure) {
            return Result.failure(
                Exception("Binary extraction failed: ${extractResult.exceptionOrNull()?.message}")
            )
        }

        val verifyResult = checksumVerifier.verifyAll(uri)
        if (verifyResult.isFailure) {
            return Result.failure(
                Exception("Binary integrity check failed: ${verifyResult.exceptionOrNull()?.message ?: "Checksum mismatch"}")
            )
        }

        settingsRepository.setContainerUri(uri)
        return Result.success(ContainerState.Ready(uri))
    }
}
