package com.pontifex.app.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pontifex.app.domain.usecase.InitProgress
import com.pontifex.app.domain.usecase.InitializeContainerUseCase
import com.pontifex.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashState {
    data object Checking : SplashState()
    data class Extracting(val progress: Float, val step: String) : SplashState()
    data class Verifying(val progress: Float) : SplashState()
    data object Ready : SplashState()
    data class Error(val message: String, val retryable: Boolean) : SplashState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val initializeContainerUseCase: InitializeContainerUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Checking)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _shouldNavigateToOnboarding = MutableStateFlow(false)
    val shouldNavigateToOnboarding: StateFlow<Boolean> = _shouldNavigateToOnboarding.asStateFlow()

    init {
        checkAndInitialize()
    }

    private fun checkAndInitialize() {
        viewModelScope.launch {
            val isOnboardingComplete = settingsRepository.isOnboardingComplete().first()

            if (!isOnboardingComplete) {
                _shouldNavigateToOnboarding.value = true
                return@launch
            }

            val containerUri = settingsRepository.getContainerUri().first()
            if (containerUri.isNullOrBlank()) {
                _shouldNavigateToOnboarding.value = true
                return@launch
            }

            val checkResult = initializeContainerUseCase.checkExisting()
            if (checkResult.isSuccess && checkResult.getOrNull() is com.pontifex.app.domain.model.ContainerState.Ready) {
                _isReady.value = true
                return@launch
            }

            _state.value = SplashState.Error(
                message = "Container not initialized. Please complete onboarding.",
                retryable = true
            )
        }
    }

    fun initializeAtUri(uri: String) {
        viewModelScope.launch {
            initializeContainerUseCase.initializeAtUri(uri).collect { progress ->
                when (progress) {
                    is InitProgress.CheckingContainer -> {
                        _state.value = SplashState.Extracting(0f, "Checking container...")
                    }
                    is InitProgress.ExtractingBinary -> {
                        _state.value = SplashState.Extracting(
                            progress.progress,
                            "Extracting ${progress.name}..."
                        )
                    }
                    is InitProgress.VerifyingChecksums -> {
                        _state.value = SplashState.Verifying(0.8f)
                    }
                    is InitProgress.Complete -> {
                        _isReady.value = true
                    }
                    is InitProgress.Failed -> {
                        _state.value = SplashState.Error(
                            message = progress.error.message,
                            retryable = true
                        )
                    }
                }
            }
        }
    }

    fun retry() {
        _state.value = SplashState.Checking
        _shouldNavigateToOnboarding.value = false
        checkAndInitialize()
    }

    fun goToOnboarding() {
        _shouldNavigateToOnboarding.value = true
    }
}
