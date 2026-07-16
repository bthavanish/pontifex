package com.pontifex.app.domain.usecase

import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ChecksumVerifier
import javax.inject.Inject

class VerifyBinaryIntegrityUseCase @Inject constructor(
    private val binaryManager: BinaryManager,
    private val checksumVerifier: ChecksumVerifier
) {
    suspend operator fun invoke(containerPath: String): Result<Boolean> {
        val verifyResult = checksumVerifier.verifyAll(containerPath)
        return verifyResult.map { true }
    }
}
