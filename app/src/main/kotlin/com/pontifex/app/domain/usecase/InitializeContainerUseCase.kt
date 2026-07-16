package com.pontifex.app.domain.usecase

import android.content.Context
import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ChecksumVerifier
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.domain.model.AppError
import com.pontifex.app.domain.model.ContainerState
import com.pontifex.app.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import javax.inject.Inject

sealed class InitProgress {
    data object CheckingContainer : InitProgress()
    data class ExtractingBinary(val name: String, val progress: Float) : InitProgress()
    data object VerifyingChecksums : InitProgress()
    data object Complete : InitProgress()
    data class Failed(val error: AppError) : InitProgress()
}

class InitializeContainerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val containerManager: ContainerManager,
    private val binaryManager: BinaryManager,
    private val checksumVerifier: ChecksumVerifier,
    private val settingsRepository: SettingsRepository
) {
    suspend fun checkExisting(): Result<ContainerState> {
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

    fun initializeAtUri(uri: String): Flow<InitProgress> = flow {
        emit(InitProgress.CheckingContainer)

        val initResult = containerManager.initializeContainer(uri)
        if (initResult.isFailure) {
            emit(InitProgress.Failed(
                AppError.ContainerInit(initResult.exceptionOrNull()?.message ?: "Unknown error")
            ))
            return@flow
        }

        val containerPath = containerManager.getContainerPath(uri)

        emit(InitProgress.ExtractingBinary("adb", 0.1f))
        val extractResult = binaryManager.extractBinaries(containerPath)
        if (extractResult.isFailure) {
            emit(InitProgress.Failed(
                AppError.BinaryIntegrity("", extractResult.exceptionOrNull()?.message ?: "Extraction failed")
            ))
            return@flow
        }
        emit(InitProgress.ExtractingBinary("fastboot", 0.6f))

        emit(InitProgress.VerifyingChecksums)
        val verifyResult = checksumVerifier.verifyAll(containerPath)
        if (verifyResult.isFailure) {
            emit(InitProgress.Failed(
                AppError.BinaryIntegrity(
                    expected = "",
                    actual = verifyResult.exceptionOrNull()?.message ?: "Checksum mismatch"
                )
            ))
            return@flow
        }

        settingsRepository.setContainerUri(uri)
        emit(InitProgress.Complete)
    }.flowOn(Dispatchers.IO)
}
