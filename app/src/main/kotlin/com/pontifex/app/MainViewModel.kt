package com.pontifex.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pontifex.app.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val isDarkMode: StateFlow<Boolean> = settingsRepository.isDarkMode()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isDynamicColor: StateFlow<Boolean> = settingsRepository.isDynamicColor()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val isAmoledBlack: StateFlow<Boolean> = settingsRepository.isAmoledBlack()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
}
