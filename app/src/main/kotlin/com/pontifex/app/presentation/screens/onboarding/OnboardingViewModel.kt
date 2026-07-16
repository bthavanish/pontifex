package com.pontifex.app.presentation.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pontifex.app.data.binary.BinaryManager
import com.pontifex.app.data.binary.ContainerManager
import com.pontifex.app.domain.repository.SettingsRepository
import com.pontifex.app.domain.usecase.InitProgress
import com.pontifex.app.domain.usecase.InitializeContainerUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val initializeContainerUseCase: InitializeContainerUseCase,
    private val containerManager: ContainerManager,
    private val binaryManager: BinaryManager
) : ViewModel() {

    private val _containerUri = MutableStateFlow<String?>(null)
    val containerUri: StateFlow<String?> = _containerUri.asStateFlow()

    private val _extractionProgress = MutableStateFlow(0f)
    val extractionProgress: StateFlow<Float> = _extractionProgress.asStateFlow()

    private val _isExtractionComplete = MutableStateFlow(false)
    val isExtractionComplete: StateFlow<Boolean> = _isExtractionComplete.asStateFlow()

    private val _extractionError = MutableStateFlow<String?>(null)
    val extractionError: StateFlow<String?> = _extractionError.asStateFlow()

    private val _isExtracting = MutableStateFlow(false)
    val isExtracting: StateFlow<Boolean> = _isExtracting.asStateFlow()

    fun setContainerUri(uri: String) {
        _containerUri.value = uri
        _extractionError.value = null
    }

    fun startExtraction() {
        val uri = _containerUri.value ?: return
        if (_isExtracting.value) return

        _isExtracting.value = true
        _extractionError.value = null
        _extractionProgress.value = 0.01f

        viewModelScope.launch {
            initializeContainerUseCase.initializeAtUri(uri).collect { progress ->
                when (progress) {
                    is InitProgress.CheckingContainer -> {
                        _extractionProgress.value = 0.05f
                    }
                    is InitProgress.ExtractingBinary -> {
                        _extractionProgress.value = progress.progress
                    }
                    is InitProgress.VerifyingChecksums -> {
                        _extractionProgress.value = 0.8f
                    }
                    is InitProgress.Complete -> {
                        _extractionProgress.value = 1f
                        _isExtractionComplete.value = true
                        _isExtracting.value = false
                    }
                    is InitProgress.Failed -> {
                        _extractionProgress.value = 0f
                        _extractionError.value = progress.error.message
                        _isExtracting.value = false
                    }
                }
            }
        }
    }

    fun clearError() {
        _extractionError.value = null
    }

    suspend fun completeOnboarding() {
        settingsRepository.setContainerUri(_containerUri.value ?: "")
        settingsRepository.setOnboardingComplete(true)
    }
}
